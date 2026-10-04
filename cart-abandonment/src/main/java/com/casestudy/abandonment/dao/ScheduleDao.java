package com.casestudy.abandonment.dao;

import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleDao {

    ScheduleJob save(ScheduleJob job);

    Optional<ScheduleJob> findById(Long scheduleId);

    Optional<ScheduleJob> findByIdempotencyKey(String idempotencyKey);

    List<ScheduleJob> findByCartId(String cartId);

    List<ScheduleJob> findByCartIdAndType(String cartId, JobType jobType);

    List<ScheduleJob> claimDue(LocalDateTime now, int limit);

    int cancelPendingByCartId(String cartId, CancellationReason reason, LocalDateTime now);

    void updateStatus(Long scheduleId, JobStatus status, CancellationReason reason, LocalDateTime now);
}
