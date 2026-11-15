package com.marketsense.insight.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {

    public GeminiRequest(String text) {
        this(text, null);
    }

    public GeminiRequest(String text, GenerationConfig generationConfig) {
        this(List.of(new Content(List.of(new Part(text)))), generationConfig);
    }

    /**
     * Builds a request that forces Gemini to reply with JSON matching {@code responseSchema}
     * (structured output), so the response can be deserialized directly.
     */
    public static GeminiRequest withJsonSchema(String text, Map<String, Object> responseSchema) {
        return new GeminiRequest(text, new GenerationConfig("application/json", responseSchema));
    }

    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerationConfig(String responseMimeType, Map<String, Object> responseSchema) {}
}
