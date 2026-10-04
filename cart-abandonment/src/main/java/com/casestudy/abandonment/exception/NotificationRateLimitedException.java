package com.casestudy.abandonment.exception;

import java.time.Duration;

public final class NotificationRateLimitedException extends NotificationProviderException {

    private final Duration retryAfter;

    public NotificationRateLimitedException(Duration retryAfter) {
        super("Notification provider rate limited (429); retry after " + retryAfter);
        this.retryAfter = retryAfter == null ? Duration.ofSeconds(60) : retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
