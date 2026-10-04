package com.casestudy.abandonment.retry;

import com.casestudy.abandonment.exception.NotificationProviderException;
import com.casestudy.abandonment.exception.NotificationRateLimitedException;

import java.time.Duration;

public final class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration baseDelay;

    public ExponentialBackoffRetryPolicy() {
        this(3, Duration.ofMinutes(1));
    }

    public ExponentialBackoffRetryPolicy(int maxAttempts, Duration baseDelay) {
        this.maxAttempts = maxAttempts;
        this.baseDelay = baseDelay;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public boolean shouldRetry(int attemptCount, NotificationProviderException error) {
        return error.retryable() && attemptCount < maxAttempts;
    }

    @Override
    public Duration delayBeforeRetry(int attemptCount, NotificationProviderException error) {
        if (error instanceof NotificationRateLimitedException rateLimited) {
            Duration retryAfter = rateLimited.retryAfter();
            if (retryAfter != null && !retryAfter.isZero() && !retryAfter.isNegative()) {
                return retryAfter;
            }
        }
        long multiplier = 1L << Math.max(0, attemptCount - 1);
        return baseDelay.multipliedBy(multiplier);
    }
}
