package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;

public record DispatchResult(JobStatus status, CancellationReason reason, String providerMessageId) {

    public static DispatchResult fired(String providerMessageId) {
        return new DispatchResult(JobStatus.FIRED, null, providerMessageId);
    }

    public static DispatchResult cancelled(CancellationReason reason) {
        return new DispatchResult(JobStatus.CANCELLED, reason, null);
    }

    public static DispatchResult failed(String detail) {
        return new DispatchResult(JobStatus.FAILED, null, detail);
    }

    public static DispatchResult retryScheduled(String retryAt) {
        return new DispatchResult(JobStatus.PENDING, null, retryAt);
    }

    public static DispatchResult deadLettered(String detail) {
        return new DispatchResult(JobStatus.FAILED, null, detail);
    }
}
