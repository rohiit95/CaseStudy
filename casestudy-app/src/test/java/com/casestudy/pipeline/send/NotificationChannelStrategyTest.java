package com.casestudy.pipeline.send;

import com.casestudy.pipeline.send.support.RecordingNotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationChannelStrategyTest {

    private final RecordingNotificationPublisher publisher = new RecordingNotificationPublisher();
    private NotificationChannelRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NotificationChannelRegistry(List.of(
                new EmailNotificationChannel(publisher),
                new SmsNotificationChannel(publisher),
                new PushNotificationChannel(publisher)
        ));
        publisher.clear();
    }

    @Test
    void eachChannelDelegatesToPublisher() {
        NotificationRequest request = new NotificationRequest(
                1L,
                "cart-1",
                "user-1",
                "cart-abandoned-default",
                NotificationChannelType.EMAIL
        );

        PublishResult email = registry.get(NotificationChannelType.EMAIL).publish(request);
        PublishResult sms = registry.get(NotificationChannelType.SMS).publish(
                new NotificationRequest(2L, "cart-1", "user-1", "tpl", NotificationChannelType.SMS)
        );
        PublishResult push = registry.get(NotificationChannelType.PUSH).publish(
                new NotificationRequest(3L, "cart-1", "user-1", "tpl", NotificationChannelType.PUSH)
        );

        assertThat(email.success()).isTrue();
        assertThat(sms.success()).isTrue();
        assertThat(push.success()).isTrue();
        assertThat(publisher.getPublished()).hasSize(3);
    }
}
