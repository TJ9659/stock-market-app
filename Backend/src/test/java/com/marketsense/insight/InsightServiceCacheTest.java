package com.marketsense.insight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsense.common.exceptions.AiAnalysisException;
import com.marketsense.config.CacheConfiguration;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies the real Caffeine cache config: successes are cached, failures are not. */
@SpringJUnitConfig(InsightServiceCacheTest.TestConfig.class)
class InsightServiceCacheTest {

    @Configuration
    @Import({CacheConfiguration.class, InsightService.class})
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @MockitoBean
    GeminiClient geminiClient;

    @MockitoBean
    StockService stockService;

    @Autowired
    InsightService insightService;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("market_insights").clear();
        CompanyNewsDto news = new CompanyNewsDto();
        news.setHeadline("Something happened");
        when(stockService.getLatestNewsFromCompany(anyString(), anyString(), anyString())).thenReturn(List.of(news));
    }

    @Test
    void successfulInsightIsCachedPerSymbol() {
        when(geminiClient.generate(any())).thenReturn(InsightServiceTest.geminiReplying(
                "{\"sentiment\":\"BULLISH\",\"catalyst\":\"x\",\"confidence\":0.8,\"keyPoints\":[]}"));

        insightService.analyzeStock("AAPL");
        insightService.analyzeStock("AAPL");
        insightService.analyzeStock("MSFT");

        verify(geminiClient, times(2)).generate(any()); // once for AAPL, once for MSFT
    }

    @Test
    void failedInsightIsNotCached() {
        when(geminiClient.generate(any()))
                .thenReturn(InsightServiceTest.geminiReplying("not json"))
                .thenReturn(InsightServiceTest.geminiReplying(
                        "{\"sentiment\":\"BEARISH\",\"catalyst\":\"x\",\"confidence\":0.6,\"keyPoints\":[]}"));

        assertThatThrownBy(() -> insightService.analyzeStock("AAPL")).isInstanceOf(AiAnalysisException.class);

        // The retry should hit Gemini again and succeed, rather than serving a cached error.
        assertThat(insightService.analyzeStock("AAPL").sentiment()).isEqualTo("BEARISH");
        verify(geminiClient, times(2)).generate(any());
    }
}
