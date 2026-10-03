package com.casestudy.pipeline.schedule;

import com.casestudy.ab.AbService;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.ab.impl.AbServiceImpl;
import com.casestudy.config.ConfigService;
import com.casestudy.models.ActivityType;
import com.casestudy.models.CartActivity;
import com.casestudy.models.CartEvent;
import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.dao.ReminderScheduleStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationScheduler {

    private final ReminderScheduleStore reminderScheduleStore;
    private final ConfigService configService;
    private final AbService abService;

    public NotificationScheduler(
            ReminderScheduleStore reminderScheduleStore,
            ConfigService configService,
            AbService abService
    ) {
        this.reminderScheduleStore = reminderScheduleStore;
        this.configService = configService;
        this.abService = abService;
    }

    @Transactional
    public List<ReminderSchedule> scheduleIfEligible(CartEvent event, CartActivity cart) {
        CartReminderVariant variant = resolveVariant(event);
        if (!shouldSchedule(event, variant)) {
            return List.of();
        }
        return scheduleReminders(cart, variant, cart.getLastActivityTime());
    }

    public List<ReminderSchedule> scheduleForDuplicate(CartActivity cart) {
        return reminderScheduleStore.findByCartIdAndActivityVersion(
                cart.getCartId(),
                cart.getActivityVersion()
        );
    }

    private boolean shouldSchedule(CartEvent event, CartReminderVariant variant) {
        if (event.getActivityType() == ActivityType.CLEAR
                || event.getActivityType() == ActivityType.PURCHASE) {
            return false;
        }
        if (!abService.isEnabled(AbServiceImpl.CART_REMINDERS_EXPERIMENT, subjectId(event))) {
            return false;
        }
        return variant.reminderEnabled();
    }

    private List<ReminderSchedule> scheduleReminders(
            CartActivity cart,
            CartReminderVariant variant,
            LocalDateTime activityTime
    ) {
        List<ReminderSchedule> reminders = new ArrayList<>();
        for (Integer window : variant.reminderWindows()) {
            ReminderSchedule reminder = new ReminderSchedule();
            reminder.setCartId(cart.getCartId());
            reminder.setActivityVersion(cart.getActivityVersion());
            reminder.setReminderWindowInMins(window);
            reminder.setMessageTemplate(variant.messageTemplate());
            reminder.setScheduledAt(activityTime.plusMinutes(window));
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setAttemptCount(0);
            reminders.add(reminder);
        }
        return reminderScheduleStore.saveAll(reminders);
    }

    private CartReminderVariant resolveVariant(CartEvent event) {
        return abService.getCartReminderVariant(
                subjectId(event),
                new CartReminderVariant(
                        configService.getReminderWindowsInMinutes(),
                        configService.getMessageTemplate(),
                        configService.isRemindersEnabled()
                )
        );
    }

    private static String subjectId(CartEvent event) {
        return event.getUserId() == null ? event.getCartId() : event.getUserId();
    }
}
