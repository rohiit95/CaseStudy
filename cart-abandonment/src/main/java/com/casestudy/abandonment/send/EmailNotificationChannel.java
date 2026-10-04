package com.casestudy.abandonment.send;

public final class EmailNotificationChannel implements NotificationChannel {

    private final NotificationPublisher publisher;

    public EmailNotificationChannel(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return publisher.publish(channelType(), request);
    }
}
