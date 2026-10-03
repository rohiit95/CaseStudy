package com.casestudy.pipeline.send.support;

import com.casestudy.pipeline.send.NotificationChannelType;
import com.casestudy.pipeline.send.NotificationPublisher;
import com.casestudy.pipeline.send.NotificationRequest;
import com.casestudy.pipeline.send.PublishResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test double for {@link NotificationPublisher}.
 */
public class RecordingNotificationPublisher implements NotificationPublisher {

    private final List<PublishedNotification> published = new CopyOnWriteArrayList<>();

    @Override
    public PublishResult publish(NotificationChannelType channelType, NotificationRequest request) {
        String providerMessageId = "test-" + channelType.name().toLowerCase() + "-" + published.size();
        published.add(new PublishedNotification(channelType, request, providerMessageId));
        return PublishResult.success(providerMessageId);
    }

    public List<PublishedNotification> getPublished() {
        return Collections.unmodifiableList(new ArrayList<>(published));
    }

    public void clear() {
        published.clear();
    }

    public record PublishedNotification(
            NotificationChannelType channelType,
            NotificationRequest request,
            String providerMessageId
    ) {
    }
}
