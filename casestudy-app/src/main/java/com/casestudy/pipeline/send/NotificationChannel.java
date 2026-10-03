package com.casestudy.pipeline.send;

/**
 * Strategy interface for notification channels. Implementations are mocks — no real provider send.
 */
public interface NotificationChannel {

    NotificationChannelType channelType();

    PublishResult publish(NotificationRequest request);
}
