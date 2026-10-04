package com.casestudy.abandonment.scheduler;

import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.model.ScheduleMetadata;
import com.casestudy.abandonment.time.Clock;

import java.time.LocalDateTime;
import java.util.List;

public final class SchedulerImpl implements Scheduler {

    private final ScheduleDao scheduleDao;
    private final Clock clock;

    public SchedulerImpl(ScheduleDao scheduleDao, Clock clock) {
        this.scheduleDao = scheduleDao;
        this.clock = clock;
    }

    @Override
    public ScheduleJob schedule(
            JobType jobType,
            String idempotencyKey,
            LocalDateTime scheduledAt,
            ScheduleMetadata metadata
    ) {
        LocalDateTime now = clock.now();
        return scheduleDao.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    existing.setScheduledAt(scheduledAt);
                    existing.setMetadata(metadata);
                    existing.setJobType(jobType);
                    existing.setStatus(JobStatus.PENDING);
                    existing.setCancellationReason(null);
                    existing.setUpdatedAt(now);
                    return scheduleDao.save(existing);
                })
                .orElseGet(() -> {
                    ScheduleJob job = new ScheduleJob();
                    job.setJobType(jobType);
                    job.setIdempotencyKey(idempotencyKey);
                    job.setScheduledAt(scheduledAt);
                    job.setMetadata(metadata);
                    job.setStatus(JobStatus.PENDING);
                    job.setAttemptCount(0);
                    job.setCreatedAt(now);
                    job.setUpdatedAt(now);
                    return scheduleDao.save(job);
                });
    }

    @Override
    public ScheduleJob reschedule(String idempotencyKey, LocalDateTime scheduledAt) {
        ScheduleJob existing = scheduleDao.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("No schedule for " + idempotencyKey));
        existing.setScheduledAt(scheduledAt);
        existing.setStatus(JobStatus.PENDING);
        existing.setCancellationReason(null);
        existing.setUpdatedAt(clock.now());
        return scheduleDao.save(existing);
    }

    @Override
    public int cancelPendingByCartId(String cartId, CancellationReason reason) {
        return scheduleDao.cancelPendingByCartId(cartId, reason, clock.now());
    }

    @Override
    public List<ScheduleJob> claimDue(int limit) {
        return scheduleDao.claimDue(clock.now(), limit);
    }

    @Override
    public List<ScheduleJob> jobsForCart(String cartId) {
        return scheduleDao.findByCartId(cartId);
    }
}
