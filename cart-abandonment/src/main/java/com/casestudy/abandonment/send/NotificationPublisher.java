package com.casestudy.abandonment.send;

public interface NotificationPublisher {

    PublishResult publish(NotificationChannelType channelType, NotificationRequest request);
}
