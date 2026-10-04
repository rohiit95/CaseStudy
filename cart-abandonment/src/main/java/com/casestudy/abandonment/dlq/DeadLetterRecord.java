package com.casestudy.abandonment.dlq;

import com.casestudy.abandonment.model.JobType;

import java.time.LocalDateTime;

public record DeadLetterRecord(
        Long scheduleId,
        String idempotencyKey,
        JobType jobType,
        String reason,
        int attemptCount,
        LocalDateTime failedAt
) {
}
