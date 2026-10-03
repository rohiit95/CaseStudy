package com.casestudy.pipeline.send;

public record NotificationRequest(
        Long reminderId,
        String cartId,
        String userId,
        String messageTemplate,
        NotificationChannelType channelType
) {
}
