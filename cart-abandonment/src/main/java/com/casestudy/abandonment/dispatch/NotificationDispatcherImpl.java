package com.casestudy.abandonment.dispatch;

import com.casestudy.ab.CartReminderVariant;
import com.casestudy.abandonment.dao.ProcessedJobDao;
import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.dlq.DeadLetterQueue;
import com.casestudy.abandonment.dlq.DeadLetterRecord;
import com.casestudy.abandonment.exception.CartAbandonmentException;
import com.casestudy.abandonment.exception.NotificationProviderException;
import com.casestudy.abandonment.exception.RetryExhaustedException;
import com.casestudy.abandonment.experiment.ExperimentResolver;
import com.casestudy.abandonment.guardrail.GuardrailDecision;
import com.casestudy.abandonment.guardrail.GuardrailEngine;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.retry.RetryPolicy;
import com.casestudy.abandonment.send.NotificationChannelType;
import com.casestudy.abandonment.send.NotificationChannelRegistry;
import com.casestudy.abandonment.send.NotificationRequest;
import com.casestudy.abandonment.send.PublishResult;
import com.casestudy.abandonment.time.Clock;

import java.time.Duration;
import java.time.LocalDateTime;

public final class NotificationDispatcherImpl implements NotificationDispatcher, JobHandler {

    private final GuardrailEngine guardrailEngine;
    private final ExperimentResolver experimentResolver;
    private final NotificationChannelRegistry channelRegistry;
    private final ScheduleDao scheduleDao;
    private final ProcessedJobDao processedJobDao;
    private final RetryPolicy retryPolicy;
    private final DeadLetterQueue deadLetterQueue;
    private final Clock clock;

    public NotificationDispatcherImpl(
            GuardrailEngine guardrailEngine,
            ExperimentResolver experimentResolver,
            NotificationChannelRegistry channelRegistry,
            ScheduleDao scheduleDao,
            ProcessedJobDao processedJobDao,
            RetryPolicy retryPolicy,
            DeadLetterQueue deadLetterQueue,
            Clock clock
    ) {
        this.guardrailEngine = guardrailEngine;
        this.experimentResolver = experimentResolver;
        this.channelRegistry = channelRegistry;
        this.scheduleDao = scheduleDao;
        this.processedJobDao = processedJobDao;
        this.retryPolicy = retryPolicy;
        this.deadLetterQueue = deadLetterQueue;
        this.clock = clock;
    }

    @Override
    public void handle(ScheduleJob job) {
        dispatch(job);
    }

    @Override
    public DispatchResult dispatch(ScheduleJob job) {
        if (processedJobDao.alreadyProcessed(firedKey(job))) {
            scheduleDao.updateStatus(
                    job.getScheduleId(),
                    JobStatus.CANCELLED,
                    CancellationReason.DUPLICATE,
                    clock.now()
            );
            return DispatchResult.cancelled(CancellationReason.DUPLICATE);
        }

        GuardrailDecision decision = guardrailEngine.evaluate(job);
        if (!decision.accepted()) {
            scheduleDao.updateStatus(
                    job.getScheduleId(),
                    JobStatus.CANCELLED,
                    decision.suppressionReason(),
                    clock.now()
            );
            return DispatchResult.cancelled(decision.suppressionReason());
        }

        String subjectId = job.getMetadata().userId() != null
                ? job.getMetadata().userId()
                : job.getMetadata().sessionId();
        CartReminderVariant variant = experimentResolver.resolve(
                subjectId != null ? subjectId : job.getMetadata().cartId()
        );
        if (!variant.reminderEnabled()) {
            scheduleDao.updateStatus(
                    job.getScheduleId(),
                    JobStatus.CANCELLED,
                    CancellationReason.HOLD_OUT,
                    clock.now()
            );
            return DispatchResult.cancelled(CancellationReason.HOLD_OUT);
        }

        NotificationRequest request = new NotificationRequest(
                job.getScheduleId(),
                job.getMetadata().cartId(),
                job.getMetadata().userId(),
                variant.messageTemplate(),
                toChannelType(variant.channel())
        );
        try {
            PublishResult published = channelRegistry.get(request.channelType()).publish(request);
            if (!published.success()) {
                return onProviderFailure(job, new NotificationProviderException(published.detail()));
            }
            processedJobDao.markProcessed(firedKey(job));
            scheduleDao.updateStatus(job.getScheduleId(), JobStatus.FIRED, null, clock.now());
            return DispatchResult.fired(published.providerMessageId());
        } catch (NotificationProviderException ex) {
            return onProviderFailure(job, ex);
        } catch (CartAbandonmentException ex) {
            return deadLetter(job, ex.getMessage(), Math.max(job.getAttemptCount(), 1));
        }
    }

    private DispatchResult onProviderFailure(ScheduleJob job, NotificationProviderException error) {
        int attempts = Math.max(job.getAttemptCount(), 1);
        if (retryPolicy.shouldRetry(attempts, error)) {
            Duration delay = retryPolicy.delayBeforeRetry(attempts, error);
            LocalDateTime retryAt = clock.now().plus(delay);
            job.setStatus(JobStatus.PENDING);
            job.setScheduledAt(retryAt);
            job.setUpdatedAt(clock.now());
            scheduleDao.save(job);
            return DispatchResult.retryScheduled(retryAt.toString());
        }
        RetryExhaustedException exhausted = new RetryExhaustedException(
                "Retries exhausted for schedule " + job.getScheduleId() + ": " + error.getMessage(),
                attempts,
                error
        );
        return deadLetter(job, exhausted.getMessage(), attempts);
    }

    private DispatchResult deadLetter(ScheduleJob job, String reason, int attempts) {
        scheduleDao.updateStatus(job.getScheduleId(), JobStatus.FAILED, null, clock.now());
        deadLetterQueue.enqueue(new DeadLetterRecord(
                job.getScheduleId(),
                job.getIdempotencyKey(),
                job.getJobType(),
                reason,
                attempts,
                clock.now()
        ));
        return DispatchResult.deadLettered(reason);
    }

    private static String firedKey(ScheduleJob job) {
        return job.getIdempotencyKey() + ":fired";
    }

    private static NotificationChannelType toChannelType(com.casestudy.ab.ReminderChannel channel) {
        if (channel == null) {
            return NotificationChannelType.EMAIL;
        }
        return switch (channel) {
            case EMAIL -> NotificationChannelType.EMAIL;
            case SMS -> NotificationChannelType.SMS;
            case PUSH -> NotificationChannelType.PUSH;
        };
    }
}
