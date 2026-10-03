package com.casestudy.pipeline.send;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes to the internal channel bus. Per the case study, v1 does not call external email/SMS/push
 * providers; a later implementation can replace this bean with a real bus client.
 */
@Component
public class ChannelBusNotificationPublisher implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(ChannelBusNotificationPublisher.class);

    @Override
    public PublishResult publish(NotificationChannelType channelType, NotificationRequest request) {
        String providerMessageId = channelType.name().toLowerCase() + "-" + UUID.randomUUID();
        log.debug(
                "Published reminderId={} cartId={} channel={} template={} providerMessageId={}",
                request.reminderId(),
                request.cartId(),
                channelType,
                request.messageTemplate(),
                providerMessageId
        );
        return PublishResult.success(providerMessageId);
    }
}
