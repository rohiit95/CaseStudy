package com.casestudy.abandonment.exception;

public final class RetryExhaustedException extends CartAbandonmentException {

    private final int attempts;

    public RetryExhaustedException(String message, int attempts, Throwable cause) {
        super(message, cause);
        this.attempts = attempts;
    }

    public int attempts() {
        return attempts;
    }
}
