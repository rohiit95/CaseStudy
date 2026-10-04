package com.casestudy.abandonment.retry;

import com.casestudy.abandonment.exception.NotificationProviderException;

import java.time.Duration;

public interface RetryPolicy {

    int maxAttempts();

    boolean shouldRetry(int attemptCount, NotificationProviderException error);

    Duration delayBeforeRetry(int attemptCount, NotificationProviderException error);
}
