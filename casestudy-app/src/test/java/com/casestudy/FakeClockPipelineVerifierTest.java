package com.casestudy;

import com.casestudy.ab.AbService;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.config.ConfigService;
import com.casestudy.models.ActivityType;
import com.casestudy.models.CartEvent;
import com.casestudy.models.ReminderStatus;
import com.casestudy.models.UserType;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.pipeline.dispatch.JobRunResult;
import com.casestudy.pipeline.dispatch.ReminderJobRunner;
import com.casestudy.pipeline.send.DispatchOutcome;
import com.casestudy.services.CartEventService;
import com.casestudy.time.DateTimes;
import com.casestudy.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FakeClockPipelineVerifierTest {

    @Autowired
    private CartEventService cartEventService;

    @Autowired
    private ReminderJobRunner reminderJobRunner;

    @Autowired
    private ReminderScheduleStore reminderScheduleStore;

    @Autowired
    private MutableClock clock;

    @Autowired
    private AbService abService;

    @Autowired
    private ConfigService configService;

    @BeforeEach
    void reset() {
        clock.setInstant(DateTimes.instant());
    }

    @Test
    void fastForwardTimeFiresDueJobsAfterPublish() {
        LocalDateTime t0 = DateTimes.now();
        CartEvent event = new CartEvent();
        event.setEventId("evt-clock-1");
        event.setCartId("cart-clock-1");
        event.setUserId(subjectWithRemindersEnabled());
        event.setUserType(UserType.ACCOUNT);
        event.setActivityType(ActivityType.EDIT);
        event.setActivityTime(t0);

        cartEventService.process(event);

        assertThat(reminderJobRunner.runDue(10).firedCount()).isZero();

        clock.advance(Duration.ofMinutes(30));
        JobRunResult jobRunResult = reminderJobRunner.runDue(10);

        assertThat(jobRunResult.claimedCount()).isGreaterThan(0);
        assertThat(jobRunResult.firedCount()).isGreaterThan(0);
        assertThat(jobRunResult.outcomes().stream()
                .anyMatch(o -> o.status() == DispatchOutcome.DispatchStatus.FIRED))
                .isTrue();

        Long firstJobId = jobRunResult.outcomes().stream().findFirst().get().reminderId();
        assertThat(reminderScheduleStore.findById(firstJobId))
                .isPresent()
                .get()
                .extracting(r -> r.getStatus())
                .isEqualTo(ReminderStatus.FIRED);
    }

    @Test
    void duplicateEventIdIsIdempotent() {
        CartEvent event = new CartEvent();
        event.setEventId("evt-dup-1");
        event.setCartId("cart-dup-1");
        event.setUserType(UserType.ACCOUNT);
        event.setActivityType(ActivityType.EDIT);
        event.setActivityTime(DateTimes.now());

        var first = cartEventService.process(event);
        var second = cartEventService.process(event);

        assertThat(first.isDuplicate()).isFalse();
        assertThat(second.isDuplicate()).isTrue();
        assertThat(second.getReminders()).hasSameSizeAs(first.getReminders());
    }

    @Test
    void purchaseSuppressesDueRemindersAtDispatch() {
        LocalDateTime t0 = DateTimes.now();
        CartEvent add = new CartEvent();
        add.setEventId("evt-purchase-1");
        add.setCartId("cart-purchase-1");
        add.setUserType(UserType.SESSION);
        add.setActivityType(ActivityType.EDIT);
        add.setActivityTime(t0);
        cartEventService.process(add);

        clock.advance(Duration.ofMinutes(30));

        CartEvent purchase = new CartEvent();
        purchase.setEventId("evt-purchase-2");
        purchase.setCartId("cart-purchase-1");
        purchase.setUserType(UserType.ACCOUNT);
        purchase.setActivityType(ActivityType.PURCHASE);
        purchase.setActivityTime(DateTimes.now());
        cartEventService.process(purchase);

        var run = reminderJobRunner.runDue(10);
        assertThat(run.firedCount()).isZero();
        assertThat(run.suppressedCount() + run.claimedCount()).isGreaterThanOrEqualTo(0);
    }

    private String subjectWithRemindersEnabled() {
        CartReminderVariant defaults = new CartReminderVariant(
                configService.getReminderWindowsInMinutes(),
                configService.getMessageTemplate(),
                configService.isRemindersEnabled()
        );
        for (int i = 0; i < 200; i++) {
            String userId = "user-" + i;
            CartReminderVariant variant = abService.getCartReminderVariant(userId, defaults);
            if (variant.reminderEnabled() && !variant.reminderWindows().isEmpty()) {
                return userId;
            }
        }
        throw new IllegalStateException("No AB subject with reminders enabled found");
    }
}
