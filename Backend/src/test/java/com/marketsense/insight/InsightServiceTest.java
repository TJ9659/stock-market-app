package com.marketsense.insight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsense.common.exceptions.AiAnalysisException;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.insight.dto.GeminiRequest;
import com.marketsense.insight.dto.GeminiResponse;
import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsightServiceTest {

    private GeminiClient geminiClient;
    private StockService stockService;
    private InsightService insightService;

    @BeforeEach
    void setUp() {
        geminiClient = mock(GeminiClient.class);
        stockService = mock(StockService.class);
        insightService = new InsightService(new ObjectMapper(), geminiClient, stockService);
    }

    static GeminiResponse geminiReplying(String text) {
        return new GeminiResponse(List.of(new GeminiResponse.Candidate(
                new GeminiResponse.Content(List.of(new GeminiResponse.Part(text))), "STOP")));
    }

    @Test
    void parsesStructuredReplyAndFillsInSymbol() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying("""
                {"sentiment":"BULLISH","catalyst":"Earnings beat","confidence":0.82,
                 "keyPoints":["Revenue up 12%","Guidance raised"]}"""));

        MarketInsightDto result = insightService.getAnalysis("AAPL", List.of("Apple beats earnings"));

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.sentiment()).isEqualTo("BULLISH");
        assertThat(result.catalyst()).isEqualTo("Earnings beat");
        assertThat(result.confidence()).isEqualTo(0.82);
        assertThat(result.keyPoints()).containsExactly("Revenue up 12%", "Guidance raised");
    }

    @Test
    void requestsJsonStructuredOutputWithSchemaAndIncludesHeadlines() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying(
                "{\"sentiment\":\"NEUTRAL\",\"catalyst\":\"Mixed\",\"confidence\":0.5,\"keyPoints\":[]}"));

        insightService.getAnalysis("TSLA", List.of("Tesla deliveries miss", "Tesla opens new factory"));

        ArgumentCaptor<GeminiRequest> captor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(captor.capture());
        GeminiRequest sent = captor.getValue();

        assertThat(sent.generationConfig().responseMimeType()).isEqualTo("application/json");
        assertThat(sent.generationConfig().responseSchema()).isEqualTo(InsightService.INSIGHT_SCHEMA);
        String prompt = sent.contents().get(0).parts().get(0).text();
        assertThat(prompt).contains("TSLA", "- Tesla deliveries miss", "- Tesla opens new factory");
    }

    @Test
    void normalizesLowercaseSentiment() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying(
                "{\"sentiment\":\"bearish\",\"catalyst\":\"Recall\",\"confidence\":0.7,\"keyPoints\":[\"Recall announced\"]}"));

        assertThat(insightService.getAnalysis("F", List.of("Ford recall")).sentiment()).isEqualTo("BEARISH");
    }

    @Test
    void skipsGeminiWhenThereAreNoHeadlines() {
        MarketInsightDto result = insightService.getAnalysis("XYZ", List.of());

        assertThat(result.sentiment()).isEqualTo("NEUTRAL");
        assertThat(result.symbol()).isEqualTo("XYZ");
        verify(geminiClient, never()).generate(any());
    }

    @Test
    void throwsOnMalformedJson() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying("Sure! Here is the analysis: BULLISH"));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("malformed JSON");
    }

    @Test
    void throwsOnEmptyOrBlockedResponse() {
        when(geminiClient.generate(any())).thenReturn(new GeminiResponse(List.of(
                new GeminiResponse.Candidate(null, "SAFETY"))));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void throwsOnNoCandidates() {
        when(geminiClient.generate(any())).thenReturn(new GeminiResponse(null));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class);
    }

    @Test
    void throwsOnUnknownSentiment() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying(
                "{\"sentiment\":\"STRONG BUY\",\"catalyst\":\"x\",\"confidence\":0.9,\"keyPoints\":[]}"));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("invalid sentiment");
    }

    @Test
    void throwsOnOutOfRangeConfidence() {
        when(geminiClient.generate(any())).thenReturn(geminiReplying(
                "{\"sentiment\":\"BULLISH\",\"catalyst\":\"x\",\"confidence\":85,\"keyPoints\":[]}"));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("confidence");
    }

    @Test
    void wrapsGeminiHttpErrors() {
        when(geminiClient.generate(any())).thenThrow(
                HttpServerErrorException.create(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                        "Service Unavailable", HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> insightService.getAnalysis("AAPL", List.of("headline")))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("503");
    }

    @Test
    void analyzeStockUsesAtMostTenHeadlinesFromTheLastWeek() {
        List<CompanyNewsDto> news = java.util.stream.IntStream.rangeClosed(1, 15).mapToObj(i -> {
            CompanyNewsDto dto = new CompanyNewsDto();
            dto.setHeadline("Headline " + i);
            return dto;
        }).toList();
        when(stockService.getLatestNewsFromCompany(eq("MSFT"), anyString(), anyString())).thenReturn(news);
        when(geminiClient.generate(any())).thenReturn(geminiReplying(
                "{\"sentiment\":\"NEUTRAL\",\"catalyst\":\"x\",\"confidence\":0.5,\"keyPoints\":[]}"));

        insightService.analyzeStock("MSFT");

        ArgumentCaptor<GeminiRequest> captor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(captor.capture());
        String prompt = captor.getValue().contents().get(0).parts().get(0).text();
        assertThat(prompt).contains("- Headline 10").doesNotContain("- Headline 11");
    }
}
