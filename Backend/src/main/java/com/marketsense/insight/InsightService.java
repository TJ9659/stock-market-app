package com.marketsense.insight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.insight.dto.GeminiRequest;
import com.marketsense.insight.dto.GeminiResponse;
import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {
    private final ObjectMapper objectMapper;
    private final GeminiClient geminiClient;
    private final StockService stockService;
    @Value("${gemini.api-key}")
    private String apiKey;

    private MarketInsightDto mapToMarketInsight(GeminiResponse response) {
        String rawText = "";
        try {
            rawText = response.getFirstText();
            if (rawText == null || rawText.isBlank()) {
                throw new RuntimeException("Empty response from AI");
            }

            String cleanedJson = rawText.replaceAll("(?s)```json\\s*", "") // Removes opening fence
                    .replaceAll("```", "").trim();

            return objectMapper.readValue(cleanedJson, MarketInsightDto.class);
        } catch (JsonProcessingException e) {
            System.err.println("!!! Jackson Parsing Failed !!!");
            System.err.println("Error details: " + e.getMessage());
            System.err.println("Attempted to parse: " + rawText);
            return new MarketInsightDto("ERROR", "NEUTRAL", "Failed to parse AI analysis", 0.0, List.of());
        }
    }

    public MarketInsightDto getAnalysis(String symbol, List<String> headlines) {

        if (headlines.isEmpty()) {
            return new MarketInsightDto(symbol, "NEUTRAL", "No recent news found to evaluate.", 0.5, List.of());
        }

        String formattedHeadlines = headlines.stream().map(h -> "- " + h).collect(Collectors.joining("\n"));

        String prompt = """
                Analyze the market sentiment for ticker symbol %s based STRICTLY on the following recent headlines:
                                
                %s
                                
                You MUST respond ONLY with a raw JSON object matching this schema exactly. 
                Do not include any introductory prose, markdown code blocks, or backticks.
                                
                {
                  "sentiment": "BULLISH", "BEARISH", or "NEUTRAL",
                  "catalyst": "A concise summary explaining the primary market driver based on the text",
                  "confidence": 0.85,
                  "keyPoints": ["Brief supporting point 1", "Brief supporting point 2"]
                }
                                
                CRITICAL CONSTRAINTS:
                - The "confidence" property MUST be a raw decimal number between 0.0 and 1.0 representing your certainty. Do NOT use text values like "Medium", "High", or "Low".
                - The "sentiment" value must be fully capitalized.
                - The "confidence" property must be a decimal between 0.0 and 1.0.
                - 1.0 should only be used if the sentiment is indisputable based on the text.
                - If headlines are conflicting or vague, the confidence should reflect that (e.g., 0.5 - 0.7).
                """.formatted(symbol, formattedHeadlines);

        GeminiRequest request = new GeminiRequest(prompt);
        GeminiResponse response = geminiClient.generate(request);

        return mapToMarketInsight(response);
    }

    @Cacheable(value = "market_insights", key = "#symbol")
    public MarketInsightDto analyzeStock(String symbol) {
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);

        String to = today.toString();
        String from = oneWeekAgo.toString();

        List<CompanyNewsDto> newsList = stockService.getLatestNewsFromCompany(symbol, from, to);

        List<String> headlines = newsList.stream().map(news -> news.getHeadline()).limit(10).toList();

        return getAnalysis(symbol, headlines);
    }
}
