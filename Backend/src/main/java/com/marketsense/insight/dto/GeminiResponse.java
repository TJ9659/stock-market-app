package com.marketsense.insight.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {
    public record Candidate(Content content, String finishReason) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    /**
     * Returns the text of the first candidate, or an empty string when Gemini returned no usable
     * content (e.g. the response was blocked, or the output was cut off before any text).
     */
    public String getFirstText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Content content = candidates.get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return "";
        }
        String text = content.parts().get(0).text();
        return text == null ? "" : text;
    }
}
