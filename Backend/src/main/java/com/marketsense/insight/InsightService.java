package com.marketsense.insight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsense.common.exceptions.AiAnalysisException;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.insight.dto.GeminiRequest;
import com.marketsense.insight.dto.GeminiResponse;
import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    static final Set<String> SENTIMENTS = Set.of("BULLISH", "BEARISH", "NEUTRAL");

    /**
     * Gemini structured-output schema. Gemini is constrained to return exactly this JSON shape,
     * so there's no need to strip markdown fences or hope the model followed the prompt.
     */
    static final Map<String, Object> INSIGHT_SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "sentiment", Map.of("type", "STRING", "enum", List.of("BULLISH", "BEARISH", "NEUTRAL")),
                    "catalyst", Map.of("type", "STRING"),
                    "confidence", Map.of("type", "NUMBER", "minimum", 0, "maximum", 1),
                    "keyPoints", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))
            ),
            "required", List.of("sentiment", "catalyst", "confidence", "keyPoints"),
            "propertyOrdering", List.of("sentiment", "catalyst", "confidence", "keyPoints")
    );

    private final ObjectMapper objectMapper;
    private final GeminiClient geminiClient;
    private final StockService stockService;

    /** Shape of the model's JSON reply; the symbol is filled in by us, not the model. */
    record AiInsight(String sentiment, String catalyst, Double confidence, List<String> keyPoints) {}

    public MarketInsightDto getAnalysis(String symbol, List<String> headlines) {

        if (headlines.isEmpty()) {
            return new MarketInsightDto(symbol, "NEUTRAL", "No recent news found to evaluate.", 0.5, List.of());
        }

        String formattedHeadlines = headlines.stream().map(h -> "- " + h).collect(Collectors.joining("\n"));

        String prompt = """
                Analyze the market sentiment for ticker symbol %s based STRICTLY on the following recent headlines:

                %s

                Guidelines:
                - "catalyst": a concise summary of the primary market driver in these headlines.
                - "keyPoints": 2-4 brief supporting points drawn from the headlines.
                - "confidence": your certainty from 0.0 to 1.0. Use 1.0 only if the sentiment is indisputable.
                  If headlines are conflicting or vague, keep it around 0.5-0.7.
                """.formatted(symbol, formattedHeadlines);

        GeminiResponse response;
        try {
            response = geminiClient.generate(GeminiRequest.withJsonSchema(prompt, INSIGHT_SCHEMA));
        } catch (RestClientResponseException e) {
            throw new AiAnalysisException("AI provider returned HTTP " + e.getStatusCode().value(), e);
        }

        return toMarketInsight(symbol, response);
    }

    private MarketInsightDto toMarketInsight(String symbol, GeminiResponse response) {
        String rawText = response == null ? "" : response.getFirstText();
        if (rawText.isBlank()) {
            throw new AiAnalysisException("AI returned an empty response for " + symbol);
        }

        AiInsight insight;
        try {
            insight = objectMapper.readValue(rawText, AiInsight.class);
        } catch (JsonProcessingException e) {
            throw new AiAnalysisException("AI returned malformed JSON for " + symbol, e);
        }

        if (insight.sentiment() == null || !SENTIMENTS.contains(insight.sentiment().toUpperCase())) {
            throw new AiAnalysisException("AI returned an invalid sentiment: " + insight.sentiment());
        }
        if (insight.confidence() == null || insight.confidence() < 0.0 || insight.confidence() > 1.0) {
            throw new AiAnalysisException("AI returned an out-of-range confidence: " + insight.confidence());
        }

        return new MarketInsightDto(
                symbol,
                insight.sentiment().toUpperCase(),
                insight.catalyst(),
                insight.confidence(),
                insight.keyPoints() == null ? List.of() : insight.keyPoints()
        );
    }

    /**
     * Cached for 30 minutes per symbol (see CacheConfiguration). Failures throw, and Spring never
     * caches a thrown exception, so a bad AI response is retried on the next request.
     */
    @Cacheable(value = "market_insights", key = "#symbol")
    public MarketInsightDto analyzeStock(String symbol) {
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);

        List<CompanyNewsDto> newsList = stockService.getLatestNewsFromCompany(symbol, oneWeekAgo.toString(), today.toString());

        List<String> headlines = newsList.stream().map(CompanyNewsDto::getHeadline).limit(10).toList();

        return getAnalysis(symbol, headlines);
    }
}
