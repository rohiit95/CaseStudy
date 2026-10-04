package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.dlq.DeadLetterQueue;
import com.casestudy.abandonment.dlq.DeadLetterRecord;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.scheduler.Scheduler;
import com.casestudy.abandonment.time.Clock;

import java.util.List;
import java.util.Objects;

public final class JobRunnerImpl implements JobRunner {

    private final JobType jobType;
    private final JobHandler handler;
    private final Scheduler scheduler;
    private final ScheduleDao scheduleDao;
    private final DeadLetterQueue deadLetterQueue;
    private final Clock clock;

    public JobRunnerImpl(
            JobType jobType,
            JobHandler handler,
            Scheduler scheduler,
            ScheduleDao scheduleDao,
            DeadLetterQueue deadLetterQueue,
            Clock clock
    ) {
        this.jobType = Objects.requireNonNull(jobType, "jobType");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.scheduler = scheduler;
        this.scheduleDao = scheduleDao;
        this.deadLetterQueue = deadLetterQueue;
        this.clock = clock;
    }

    @Override
    public JobType jobType() {
        return jobType;
    }

    @Override
    public JobRunSummary runDue(int limit) {
        return runDue(jobType, limit);
    }

    @Override
    public JobRunSummary runDue(JobType requestedType, int limit) {
        if (requestedType == null) {
            throw new IllegalArgumentException("jobType is required");
        }
        if (requestedType != jobType) {
            throw new IllegalArgumentException(
                    "Runner for " + jobType + " cannot claim " + requestedType + " jobs"
            );
        }
        List<ScheduleJob> claimed = scheduler.claimDue(jobType, limit);
        int handled = 0;
        int failed = 0;
        for (ScheduleJob job : claimed) {
            try {
                handler.handle(job);
                handled++;
            } catch (RuntimeException ex) {
                failed++;
                scheduleDao.updateStatus(job.getScheduleId(), JobStatus.FAILED, null, clock.now());
                deadLetterQueue.enqueue(new DeadLetterRecord(
                        job.getScheduleId(),
                        job.getIdempotencyKey(),
                        job.getJobType(),
                        ex.getMessage(),
                        job.getAttemptCount(),
                        clock.now()
                ));
            }
        }
        return new JobRunSummary(claimed.size(), handled, failed);
    }
}
