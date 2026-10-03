package com.casestudy.pipeline.send;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.dao.CartActivityStore;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.pipeline.dispatch.FireTimeGuard;
import com.casestudy.time.DateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDispatcher {

    private final ReminderScheduleStore reminderScheduleStore;
    private final CartActivityStore cartActivityStore;
    private final FireTimeGuard fireTimeGuard;
    private final NotificationChannelRegistry channelRegistry;

    public NotificationDispatcher(
            ReminderScheduleStore reminderScheduleStore,
            CartActivityStore cartActivityStore,
            FireTimeGuard fireTimeGuard,
            NotificationChannelRegistry channelRegistry
    ) {
        this.reminderScheduleStore = reminderScheduleStore;
        this.cartActivityStore = cartActivityStore;
        this.fireTimeGuard = fireTimeGuard;
        this.channelRegistry = channelRegistry;
    }

    @Transactional
    public DispatchOutcome dispatch(ReminderSchedule claimedJob) {
        FireTimeGuard.SuppressionReason reason = fireTimeGuard.evaluate(claimedJob);
        if (reason != FireTimeGuard.SuppressionReason.NONE) {
            reminderScheduleStore.updateStatus(claimedJob, ReminderStatus.CANCELLED, DateTimes.now());
            return DispatchOutcome.suppressed(claimedJob.getReminderId(), reason.name());
        }

        String userId = cartActivityStore.findByCartId(claimedJob.getCartId())
                .map(c -> c.getUserId())
                .orElse(null);

        NotificationRequest request = new NotificationRequest(
                claimedJob.getReminderId(),
                claimedJob.getCartId(),
                userId,
                claimedJob.getMessageTemplate(),
                NotificationChannelType.EMAIL
        );

        NotificationChannel channel = channelRegistry.get(request.channelType());
        PublishResult publishResult = channel.publish(request);

        if (!publishResult.success()) {
            reminderScheduleStore.markFailed(claimedJob, DateTimes.now());
            return DispatchOutcome.failed(claimedJob.getReminderId(), publishResult.detail());
        }

        reminderScheduleStore.markProcessed(claimedJob, DateTimes.now());
        return DispatchOutcome.fired(claimedJob.getReminderId(), publishResult.providerMessageId());
    }
}
