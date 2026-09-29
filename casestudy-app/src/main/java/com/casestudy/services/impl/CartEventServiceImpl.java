package com.casestudy.services.impl;

import com.casestudy.ab.AbService;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.ab.impl.AbServiceImpl;
import com.casestudy.config.ConfigService;
import com.casestudy.dao.CartActivityDao;
import com.casestudy.dao.ReminderScheduleDao;
import com.casestudy.models.ActivityType;
import com.casestudy.models.CartActivity;
import com.casestudy.models.CartActivityState;
import com.casestudy.models.CartEvent;
import com.casestudy.models.CartEventResult;
import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.services.CartEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartEventServiceImpl implements CartEventService {

    private final CartActivityDao cartActivityDao;
    private final ReminderScheduleDao reminderScheduleDao;
    private final ConfigService configService;
    private final AbService abService;

    public CartEventServiceImpl(
            CartActivityDao cartActivityDao,
            ReminderScheduleDao reminderScheduleDao,
            ConfigService configService,
            AbService abService
    ) {
        this.cartActivityDao = cartActivityDao;
        this.reminderScheduleDao = reminderScheduleDao;
        this.configService = configService;
        this.abService = abService;
    }

    @Override
    @Transactional
    public CartEventResult process(CartEvent event) {
        Optional<CartActivity> existing = cartActivityDao.findByCartId(event.getCartId());
        if (existing.isPresent() && event.getEventId().equals(existing.get().getLastEventId())) {
            CartActivity cart = existing.get();
            List<ReminderSchedule> reminders = reminderScheduleDao.findByCartIdAndActivityVersion(
                    cart.getCartId(),
                    cart.getActivityVersion()
            );
            return CartEventResult.from(cart, reminders, true);
        }

        LocalDateTime activityTime = event.getActivityTime();
        CartActivity cart = existing.orElseGet(CartActivity::new);
        boolean isNew = cart.getCartId() == null;

        if (isNew) {
            cart.setCartId(event.getCartId());
            cart.setActivityVersion(1);
        } else {
            reminderScheduleDao.cancelPendingByCartId(cart.getCartId(), activityTime);
            cart.setActivityVersion(cart.getActivityVersion() + 1);
        }

        cart.setUserId(event.getUserId());
        cart.setLastActivityTime(activityTime);
        cart.setLastEventId(event.getEventId());
        cart.setCartActivityCol(event.getUserType().name());
        cart.setState(event.getActivityType() == ActivityType.CLEAR
                ? CartActivityState.CLEARED
                : CartActivityState.ACTIVE);

        cart = cartActivityDao.save(cart);

        List<ReminderSchedule> reminders = List.of();
        CartReminderVariant variant = cartReminderVariant(event);
        if (shouldScheduleReminders(event, variant)) {
            reminders = scheduleReminders(cart, variant, activityTime);
        }

        return CartEventResult.from(cart, reminders, false);
    }

    private boolean shouldScheduleReminders(CartEvent event, CartReminderVariant variant) {
        if (event.getActivityType() == ActivityType.CLEAR) {
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
        return reminderScheduleDao.saveAll(reminders);
    }

    private CartReminderVariant cartReminderVariant(CartEvent event) {
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
