package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.dlq.DeadLetterQueue;
import com.casestudy.abandonment.dlq.DeadLetterRecord;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.scheduler.Scheduler;
import com.casestudy.abandonment.time.Clock;

import java.util.List;

public final class JobRunnerImpl implements JobRunner {

    private final Scheduler scheduler;
    private final JobHandlerRegistry handlers;
    private final ScheduleDao scheduleDao;
    private final DeadLetterQueue deadLetterQueue;
    private final Clock clock;

    public JobRunnerImpl(
            Scheduler scheduler,
            JobHandlerRegistry handlers,
            ScheduleDao scheduleDao,
            DeadLetterQueue deadLetterQueue,
            Clock clock
    ) {
        this.scheduler = scheduler;
        this.handlers = handlers;
        this.scheduleDao = scheduleDao;
        this.deadLetterQueue = deadLetterQueue;
        this.clock = clock;
    }

    @Override
    public JobRunSummary runDue(int limit) {
        List<ScheduleJob> claimed = scheduler.claimDue(limit);
        int handled = 0;
        int failed = 0;
        for (ScheduleJob job : claimed) {
            try {
                handlers.handle(job);
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
