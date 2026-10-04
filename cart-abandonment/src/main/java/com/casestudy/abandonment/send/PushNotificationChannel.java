package com.casestudy.abandonment.send;

public final class PushNotificationChannel implements NotificationChannel {

    private final NotificationPublisher publisher;

    public PushNotificationChannel(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.PUSH;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return publisher.publish(channelType(), request);
    }
}
