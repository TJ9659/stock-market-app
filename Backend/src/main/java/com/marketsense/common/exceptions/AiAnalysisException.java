package com.marketsense.common.exceptions;

/**
 * Thrown when the AI provider fails or returns output we can't use. Throwing (instead of returning
 * a placeholder result) also keeps failures out of the {@code market_insights} cache.
 */
public class AiAnalysisException extends RuntimeException {
    public AiAnalysisException(String message) {
        super(message);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
