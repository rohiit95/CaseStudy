package com.casestudy.pipeline.send;

import org.springframework.stereotype.Component;

@Component
public class PushNotificationChannel implements NotificationChannel {

    private final NotificationPublisher notificationPublisher;

    public PushNotificationChannel(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.PUSH;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return notificationPublisher.publish(channelType(), request);
    }
}
