package com.casestudy.pipeline.send;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationChannelRegistry {

    private final Map<NotificationChannelType, NotificationChannel> channels = new EnumMap<>(NotificationChannelType.class);

    public NotificationChannelRegistry(List<NotificationChannel> channelList) {
        for (NotificationChannel channel : channelList) {
            channels.put(channel.channelType(), channel);
        }
    }

    public NotificationChannel get(NotificationChannelType type) {
        NotificationChannel channel = channels.get(type);
        if (channel == null) {
            throw new IllegalArgumentException("No channel registered for " + type);
        }
        return channel;
    }
}
