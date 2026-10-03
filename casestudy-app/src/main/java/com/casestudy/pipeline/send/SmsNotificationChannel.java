package com.casestudy.pipeline.send;

import org.springframework.stereotype.Component;

@Component
public class SmsNotificationChannel implements NotificationChannel {

    private final NotificationPublisher notificationPublisher;

    public SmsNotificationChannel(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.SMS;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return notificationPublisher.publish(channelType(), request);
    }
}
