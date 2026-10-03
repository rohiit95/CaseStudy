package com.casestudy.pipeline.send;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    private final NotificationPublisher notificationPublisher;

    public EmailNotificationChannel(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return notificationPublisher.publish(channelType(), request);
    }
}
