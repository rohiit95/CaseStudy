package com.casestudy.abandonment.model;

import java.time.LocalDateTime;

public final class ScheduleJob {

    private Long scheduleId;
    private LocalDateTime scheduledAt;
    private String idempotencyKey;
    private JobType jobType;
    private ScheduleMetadata metadata;
    private JobStatus status;
    private CancellationReason cancellationReason;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public ScheduleMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ScheduleMetadata metadata) {
        this.metadata = metadata;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static ScheduleJob copyOf(ScheduleJob source) {
        if (source == null) {
            return null;
        }
        ScheduleJob copy = new ScheduleJob();
        copy.scheduleId = source.scheduleId;
        copy.scheduledAt = source.scheduledAt;
        copy.idempotencyKey = source.idempotencyKey;
        copy.jobType = source.jobType;
        copy.metadata = source.metadata;
        copy.status = source.status;
        copy.cancellationReason = source.cancellationReason;
        copy.attemptCount = source.attemptCount;
        copy.createdAt = source.createdAt;
        copy.updatedAt = source.updatedAt;
        return copy;
    }
}
