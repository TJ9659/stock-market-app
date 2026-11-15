package com.marketsense.common.exceptions;

public class EmptyWatchlistException extends RuntimeException {
    public EmptyWatchlistException(String message) {
        super(message);
    }
}
