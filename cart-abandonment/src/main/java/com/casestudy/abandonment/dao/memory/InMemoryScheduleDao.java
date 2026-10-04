package com.casestudy.abandonment.dao.memory;

import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public final class InMemoryScheduleDao implements ScheduleDao {

    private final AtomicLong ids = new AtomicLong(1);
    private final Map<Long, ScheduleJob> timeIndexedScheduleJobsMap = new ConcurrentHashMap<>();
    private final Map<String, Long> idByKey = new ConcurrentHashMap<>();
    private final ReentrantLock claimLock = new ReentrantLock();

    @Override
    public ScheduleJob save(ScheduleJob job) {
        ScheduleJob copy = ScheduleJob.copyOf(job);
        if (copy.getScheduleId() == null) {
            Long existing = idByKey.get(copy.getIdempotencyKey());
            if (existing != null) {
                copy.setScheduleId(existing);
            } else {
                copy.setScheduleId(ids.getAndIncrement());
            }
        }
        timeIndexedScheduleJobsMap.put(copy.getScheduleId(), copy);
        idByKey.put(copy.getIdempotencyKey(), copy.getScheduleId());
        return ScheduleJob.copyOf(copy);
    }

    @Override
    public Optional<ScheduleJob> findById(Long scheduleId) {
        return Optional.ofNullable(ScheduleJob.copyOf(timeIndexedScheduleJobsMap.get(scheduleId)));
    }

    @Override
    public Optional<ScheduleJob> findByIdempotencyKey(String idempotencyKey) {
        Long id = idByKey.get(idempotencyKey);
        return id == null ? Optional.empty() : findById(id);
    }

    @Override
    public List<ScheduleJob> findByCartId(String cartId) {
        return timeIndexedScheduleJobsMap.values().stream()
                .filter(job -> cartId.equals(job.getMetadata().cartId()))
                .sorted(Comparator.comparing(ScheduleJob::getScheduledAt))
                .map(ScheduleJob::copyOf)
                .toList();
    }

    @Override
    public List<ScheduleJob> claimDue(JobType jobType, LocalDateTime now, int limit) {
        if (jobType == null) {
            throw new IllegalArgumentException("jobType is required to claim jobs");
        }
        claimLock.lock();
        try {
            List<ScheduleJob> due = timeIndexedScheduleJobsMap.values().stream()
                    .filter(job -> job.getStatus() == JobStatus.PENDING)
                    .filter(job -> job.getJobType() == jobType)
                    .filter(job -> !job.getScheduledAt().isAfter(now))
                    .sorted(Comparator.comparing(ScheduleJob::getScheduledAt).thenComparing(ScheduleJob::getScheduleId))
                    .limit(limit)
                    .toList();
            List<ScheduleJob> claimed = new ArrayList<>();
            for (ScheduleJob job : due) {
                job.setStatus(JobStatus.CLAIMED);
                job.setUpdatedAt(now);
                job.setAttemptCount(job.getAttemptCount() + 1);
                claimed.add(ScheduleJob.copyOf(job));
            }
            return claimed;
        } finally {
            claimLock.unlock();
        }
    }

    @Override
    public void updateStatus(Long scheduleId, JobStatus status, CancellationReason reason, LocalDateTime now) {
        ScheduleJob job = timeIndexedScheduleJobsMap.get(scheduleId);
        if (job == null) {
            throw new IllegalStateException("Unknown schedule " + scheduleId);
        }
        job.setStatus(status);
        job.setCancellationReason(reason);
        job.setUpdatedAt(now);
    }
}
