package com.casestudy.pipeline.send;

/**
 * Outbound port for handing a notification to the delivery layer (message bus / providers).
 */
public interface NotificationPublisher {

    PublishResult publish(NotificationChannelType channelType, NotificationRequest request);
}
