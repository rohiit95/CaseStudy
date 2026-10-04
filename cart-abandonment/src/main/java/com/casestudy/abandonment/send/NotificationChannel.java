package com.casestudy.abandonment.send;

public interface NotificationChannel {

    NotificationChannelType channelType();

    PublishResult publish(NotificationRequest request);
}
