package com.casestudy.abandonment.dispatch;

import com.casestudy.ab.CartReminderVariant;
import com.casestudy.abandonment.dao.ProcessedJobDao;
import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.experiment.ExperimentResolver;
import com.casestudy.abandonment.guardrail.GuardrailDecision;
import com.casestudy.abandonment.guardrail.GuardrailEngine;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.send.NotificationChannelType;
import com.casestudy.abandonment.send.NotificationChannelRegistry;
import com.casestudy.abandonment.send.NotificationRequest;
import com.casestudy.abandonment.send.PublishResult;
import com.casestudy.abandonment.time.Clock;

public final class NotificationDispatcherImpl implements NotificationDispatcher, JobHandler {

    private final GuardrailEngine guardrailEngine;
    private final ExperimentResolver experimentResolver;
    private final NotificationChannelRegistry channelRegistry;
    private final ScheduleDao scheduleDao;
    private final ProcessedJobDao processedJobDao;
    private final Clock clock;

    public NotificationDispatcherImpl(
            GuardrailEngine guardrailEngine,
            ExperimentResolver experimentResolver,
            NotificationChannelRegistry channelRegistry,
            ScheduleDao scheduleDao,
            ProcessedJobDao processedJobDao,
            Clock clock
    ) {
        this.guardrailEngine = guardrailEngine;
        this.experimentResolver = experimentResolver;
        this.channelRegistry = channelRegistry;
        this.scheduleDao = scheduleDao;
        this.processedJobDao = processedJobDao;
        this.clock = clock;
    }

    @Override
    public void handle(ScheduleJob job) {
        dispatch(job);
    }

    @Override
    public DispatchResult dispatch(ScheduleJob job) {
        if (!processedJobDao.markProcessed(job.getIdempotencyKey() + ":dispatch")) {
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
        PublishResult published = channelRegistry.get(request.channelType()).publish(request);
        if (!published.success()) {
            scheduleDao.updateStatus(job.getScheduleId(), JobStatus.FAILED, null, clock.now());
            return DispatchResult.failed(published.detail());
        }
        scheduleDao.updateStatus(job.getScheduleId(), JobStatus.FIRED, null, clock.now());
        return DispatchResult.fired(published.providerMessageId());
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
