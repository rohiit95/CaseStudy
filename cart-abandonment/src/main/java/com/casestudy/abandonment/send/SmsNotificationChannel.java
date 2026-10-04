package com.casestudy.abandonment.send;

public final class SmsNotificationChannel implements NotificationChannel {

    private final NotificationPublisher publisher;

    public SmsNotificationChannel(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public NotificationChannelType channelType() {
        return NotificationChannelType.SMS;
    }

    @Override
    public PublishResult publish(NotificationRequest request) {
        return publisher.publish(channelType(), request);
    }
}
