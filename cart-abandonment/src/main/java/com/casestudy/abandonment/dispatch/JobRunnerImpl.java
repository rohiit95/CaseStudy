package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.scheduler.Scheduler;

import java.util.List;

public final class JobRunnerImpl implements JobRunner {

    private final Scheduler scheduler;
    private final JobHandlerRegistry handlers;

    public JobRunnerImpl(Scheduler scheduler, JobHandlerRegistry handlers) {
        this.scheduler = scheduler;
        this.handlers = handlers;
    }

    @Override
    public JobRunSummary runDue(int limit) {
        List<ScheduleJob> claimed = scheduler.claimDue(limit);
        for (ScheduleJob job : claimed) {
            handlers.handle(job);
        }
        return new JobRunSummary(claimed.size());
    }
}
