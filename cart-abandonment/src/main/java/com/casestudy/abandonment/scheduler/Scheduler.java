package com.casestudy.abandonment.scheduler;

import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.model.ScheduleMetadata;

import java.time.LocalDateTime;
import java.util.List;

public interface Scheduler {

    ScheduleJob schedule(
            JobType jobType,
            String idempotencyKey,
            LocalDateTime scheduledAt,
            ScheduleMetadata metadata
    );

    ScheduleJob reschedule(String idempotencyKey, LocalDateTime scheduledAt);

    boolean cancel(String idempotencyKey, CancellationReason reason);

    List<ScheduleJob> claimDue(JobType jobType, int limit);
}
