package com.casestudy.abandonment.send;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RecordingNotificationPublisher implements NotificationPublisher {

    private final List<Published> published = new CopyOnWriteArrayList<>();

    @Override
    public PublishResult publish(NotificationChannelType channelType, NotificationRequest request) {
        String id = channelType.name().toLowerCase() + "-" + published.size();
        published.add(new Published(channelType, request, id));
        return PublishResult.ok(id);
    }

    public List<Published> getPublished() {
        return Collections.unmodifiableList(new ArrayList<>(published));
    }

    public record Published(
            NotificationChannelType channelType,
            NotificationRequest request,
            String providerMessageId
    ) {
    }
}
