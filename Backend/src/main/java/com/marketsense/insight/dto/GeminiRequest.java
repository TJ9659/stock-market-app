package com.marketsense.insight.dto;

import java.util.List;

public record GeminiRequest(List<Content> contents) {
    public GeminiRequest(String text) {
        this(List.of(new Content(List.of(new Part(text)))));
    }

    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
