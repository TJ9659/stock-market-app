package com.marketsense.insight;

import com.marketsense.insight.dto.GeminiRequest;
import com.marketsense.insight.dto.GeminiResponse;
import feign.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class GeminiClient {
    @Value("${gemini.api-key}")
    private String apiKey;
    private Client client;

    private final RestClient restClient;

    public GeminiClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(requestFactory)
                .build();
    }

    public GeminiResponse generate(GeminiRequest request) {
//        String rawJson = restClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/models/gemini-flash-latest:GenerateContent")
//                        .queryParam("key", apiKey)
//                        .build())
//                        .body(request)
//                .retrieve()
//                .body(String.class);
//        System.out.println(rawJson);
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/gemini-3.5-flash-lite:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);
    }
}
