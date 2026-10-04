package com.casestudy.abandonment.reminder;

import com.casestudy.ab.CartReminderVariant;
import com.casestudy.abandonment.detect.CartEventProcessorImpl;
import com.casestudy.abandonment.experiment.ExperimentResolver;
import com.casestudy.abandonment.model.CartActivity;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.model.ScheduleMetadata;
import com.casestudy.abandonment.scheduler.Scheduler;
import com.casestudy.config.ConfigService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class NotificationSchedulerImpl implements NotificationScheduler {

    private final Scheduler scheduler;
    private final ExperimentResolver experimentResolver;
    private final ConfigService configService;

    public NotificationSchedulerImpl(
            Scheduler scheduler,
            ExperimentResolver experimentResolver,
            ConfigService configService
    ) {
        this.scheduler = scheduler;
        this.experimentResolver = experimentResolver;
        this.configService = configService;
    }

    @Override
    public List<ScheduleJob> scheduleReminders(CartActivity cart) {
        String subjectId = cart.getUserId() != null ? cart.getUserId() : cart.getSessionId();
        if (subjectId == null) {
            throw new IllegalArgumentException("Neither UserId nor SessionId present in cart: " + cart);
        }
        CartReminderVariant variant = experimentResolver.resolve(subjectId);
        if (!variant.reminderEnabled() || variant.reminderWindows().isEmpty()) {
            return List.of();
        }
        int abandonmentWindow = configService.getAbandonmentWindowInMinutes();
        List<ScheduleJob> jobs = new ArrayList<>();
        for (Integer window : variant.reminderWindows()) {
            LocalDateTime fireAt = cart.getLastActivityTime()
                    .plusMinutes(abandonmentWindow)
                    .plusMinutes(window);
            ScheduleMetadata metadata = new ScheduleMetadata(
                    cart.getCartId(),
                    cart.getUserId(),
                    cart.getSessionId(),
                    cart.getUserType(),
                    cart.getCartVersion(),
                    window
            );
            jobs.add(scheduler.schedule(
                    JobType.REMINDER,
                    reminderKey(cart.getCartId(), cart.getCartVersion(), window),
                    fireAt,
                    metadata
            ));
        }
        return jobs;
    }

    private String reminderKey(String cartId, int version, int window) {
        return cartId + ":" + version + ":reminder:" + window;
    }
}
