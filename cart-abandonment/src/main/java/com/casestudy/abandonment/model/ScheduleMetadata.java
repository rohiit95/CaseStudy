package com.casestudy.abandonment.model;

public record ScheduleMetadata(
        String cartId,
        String userId,
        String sessionId,
        UserType userType,
        int cartVersion,
        Integer reminderWindowInMins
) {
}
