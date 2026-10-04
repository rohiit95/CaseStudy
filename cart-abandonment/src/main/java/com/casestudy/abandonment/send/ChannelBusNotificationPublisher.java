package com.casestudy.abandonment.send;

import java.util.UUID;

public final class ChannelBusNotificationPublisher implements NotificationPublisher {

    @Override
    public PublishResult publish(NotificationChannelType channelType, NotificationRequest request) {
        return PublishResult.ok(channelType.name().toLowerCase() + "-" + UUID.randomUUID());
    }
}
