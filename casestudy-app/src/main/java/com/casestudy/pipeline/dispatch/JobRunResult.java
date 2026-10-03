package com.casestudy.pipeline.dispatch;

import com.casestudy.pipeline.send.DispatchOutcome;

import java.util.List;

public record JobRunResult(int claimedCount, List<DispatchOutcome> outcomes) {

    public static JobRunResult from(int claimedCount, List<DispatchOutcome> outcomes) {
        return new JobRunResult(claimedCount, outcomes);
    }

    public long firedCount() {
        return outcomes.stream()
                .filter(o -> o.status() == DispatchOutcome.DispatchStatus.FIRED)
                .count();
    }

    public long suppressedCount() {
        return outcomes.stream()
                .filter(o -> o.status() == DispatchOutcome.DispatchStatus.SUPPRESSED)
                .count();
    }
}
