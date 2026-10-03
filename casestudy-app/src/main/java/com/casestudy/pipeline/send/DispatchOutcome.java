package com.casestudy.pipeline.send;

public record DispatchOutcome(
        Long reminderId,
        DispatchStatus status,
        String detail
) {

    public enum DispatchStatus {
        FIRED,
        SUPPRESSED,
        FAILED
    }

    public static DispatchOutcome fired(Long reminderId, String providerMessageId) {
        return new DispatchOutcome(reminderId, DispatchStatus.FIRED, providerMessageId);
    }

    public static DispatchOutcome suppressed(Long reminderId, String reason) {
        return new DispatchOutcome(reminderId, DispatchStatus.SUPPRESSED, reason);
    }

    public static DispatchOutcome failed(Long reminderId, String detail) {
        return new DispatchOutcome(reminderId, DispatchStatus.FAILED, detail);
    }
}
