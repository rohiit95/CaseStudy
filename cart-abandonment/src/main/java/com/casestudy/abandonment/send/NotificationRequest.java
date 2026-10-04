package com.casestudy.abandonment.send;

public record NotificationRequest(
        Long scheduleId,
        String cartId,
        String userId,
        String template,
        NotificationChannelType channelType
) {
}
