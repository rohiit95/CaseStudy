package com.casestudy.pipeline.send;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelBusNotificationPublisherTest {

    private final ChannelBusNotificationPublisher publisher = new ChannelBusNotificationPublisher();

    @Test
    void returnsSuccessWithProviderMessageId() {
        NotificationRequest request = new NotificationRequest(
                99L,
                "cart-1",
                "user-1",
                "cart-abandoned-default",
                NotificationChannelType.EMAIL
        );

        PublishResult result = publisher.publish(NotificationChannelType.EMAIL, request);

        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).startsWith("email-");
    }
}
