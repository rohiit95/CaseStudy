package com.casestudy.abandonment.send;

import com.casestudy.abandonment.exception.NotificationRateLimitedException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test/simulation publisher: first {@code failuresBeforeSuccess} publishes throw 429, then delegates.
 */
public final class RateLimitedNotificationPublisher implements NotificationPublisher {

    private final NotificationPublisher delegate;
    private final int failuresBeforeSuccess;
    private final Duration retryAfter;
    private final AtomicInteger attempts = new AtomicInteger();

    public RateLimitedNotificationPublisher(
            NotificationPublisher delegate,
            int failuresBeforeSuccess,
            Duration retryAfter
    ) {
        this.delegate = delegate;
        this.failuresBeforeSuccess = failuresBeforeSuccess;
        this.retryAfter = retryAfter;
    }

    public RateLimitedNotificationPublisher(int failuresBeforeSuccess) {
        this(new ChannelBusNotificationPublisher(), failuresBeforeSuccess, Duration.ofMinutes(1));
    }

    @Override
    public PublishResult publish(NotificationChannelType channelType, NotificationRequest request) {
        int attempt = attempts.incrementAndGet();
        if (attempt <= failuresBeforeSuccess) {
            throw new NotificationRateLimitedException(retryAfter);
        }
        return delegate.publish(channelType, request);
    }

    public int attempts() {
        return attempts.get();
    }
}
