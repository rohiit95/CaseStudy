package com.casestudy.abandonment;

import com.casestudy.ab.AbService;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.abandonment.model.ActivityType;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.CartEvent;
import com.casestudy.abandonment.model.CartState;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ProcessResultType;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.model.UserType;
import com.casestudy.abandonment.time.FakeClock;
import com.casestudy.config.impl.ConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartAbandonmentPipelineTest {

    private FakeClock clock;
    private CartAbandonmentModule module;
    private ConfigServiceImpl config;
    private FixedAbService abService;

    @BeforeEach
    void setUp() {
        clock = FakeClock.startedAt(Instant.parse("2026-10-04T00:00:00Z"));
        config = new ConfigServiceImpl();
        abService = new FixedAbService(true, List.of(30, 60, 1440), "cart-abandoned-default");
        module = new CartAbandonmentModule(clock, config, abService);
    }

    @Test
    void processesCartEventAndPersistsActivity() {
        var result = module.cartEventProcessor().process(edit("evt-1", "cart-1", "user-1"));

        assertThat(result.type()).isEqualTo(ProcessResultType.CREATED);
        assertThat(result.cart().getCartVersion()).isEqualTo(1);
        assertThat(result.cart().getState()).isEqualTo(CartState.ACTIVE);
        assertThat(result.cart().getLastEventId()).isEqualTo("evt-1");
        assertThat(module.scheduler().jobsForCart("cart-1"))
                .extracting(ScheduleJob::getJobType)
                .containsExactly(JobType.ABANDONMENT_CONFIRM);
    }

    @Test
    void duplicateEventIdIsIdempotent() {
        CartEvent event = edit("evt-dup", "cart-dup", "user-1");
        module.cartEventProcessor().process(event);
        var second = module.cartEventProcessor().process(event);

        assertThat(second.type()).isEqualTo(ProcessResultType.DUPLICATE);
        assertThat(module.scheduler().jobsForCart("cart-dup")).hasSize(1);
    }

    @Test
    void debounceUpdatesExistingAbandonmentJobWithoutBumpingVersion() {
        module.cartEventProcessor().process(edit("e1", "cart-db", "user-1"));
        clock.advance(Duration.ofMinutes(5));
        var updated = module.cartEventProcessor().process(edit("e2", "cart-db", "user-1"));

        assertThat(updated.cart().getCartVersion()).isEqualTo(1);
        List<ScheduleJob> jobs = module.scheduler().jobsForCart("cart-db").stream()
                .filter(job -> job.getJobType() == JobType.ABANDONMENT_CONFIRM)
                .toList();
        assertThat(jobs).hasSize(1);
        assertThat(jobs.stream().findFirst().get().getScheduledAt()).isEqualTo(clock.now().plusMinutes(30));
    }

    @Test
    void editAfterDebounceBumpsVersionAndReschedules() {
        module.cartEventProcessor().process(edit("e1", "cart-v", "user-1"));
        clock.advance(Duration.ofMinutes(11));
        var updated = module.cartEventProcessor().process(edit("e2", "cart-v", "user-1"));

        assertThat(updated.cart().getCartVersion()).isEqualTo(2);
        List<ScheduleJob> jobs = module.scheduler().jobsForCart("cart-v");
        assertThat(jobs.stream().filter(j -> j.getStatus() == JobStatus.CANCELLED)).isNotEmpty();
        assertThat(jobs.stream().filter(j -> j.getStatus() == JobStatus.PENDING)).hasSize(1);
        assertThat(jobs.stream().filter(j -> j.getStatus() == JobStatus.PENDING).findFirst().orElseThrow()
                .getMetadata().cartVersion()).isEqualTo(2);
    }

    @Test
    void fakeClockTriggersAbandonmentThenReminders() {
        module.cartEventProcessor().process(edit("e1", "cart-time", "user-1"));

        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);
        assertThat(jobs("cart-time", JobType.ABANDONMENT_CONFIRM, JobStatus.FIRED)).hasSize(1);
        assertThat(jobs("cart-time", JobType.REMINDER, JobStatus.PENDING)).hasSize(3);

        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);
        assertThat(jobs("cart-time", JobType.REMINDER, JobStatus.FIRED)).hasSize(1);
        assertThat(jobs("cart-time", JobType.REMINDER, JobStatus.PENDING)).hasSize(2);
    }

    @Test
    void purchaseSuppressesPendingReminders() {
        module.cartEventProcessor().process(edit("e1", "cart-buy", "user-1"));
        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);

        CartEvent purchase = edit("e-buy", "cart-buy", "user-1");
        purchase.setActivityType(ActivityType.PURCHASE);
        module.cartEventProcessor().process(purchase);

        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);

        assertThat(module.cartActivityDao().findByCartId("cart-buy").orElseThrow().getState())
                .isEqualTo(CartState.PURCHASED);
        assertThat(jobs("cart-buy", JobType.REMINDER, JobStatus.FIRED)).isEmpty();
        assertThat(jobs("cart-buy", JobType.REMINDER, JobStatus.CANCELLED))
                .allMatch(job -> job.getCancellationReason() == CancellationReason.PURCHASED);
    }

    @Test
    void fireTimeGuardSuppressesStaleVersionAfterPurchase() {
        module.cartEventProcessor().process(edit("e1", "cart-race", "user-1"));
        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);
        ScheduleJob reminder = jobs("cart-race", JobType.REMINDER, JobStatus.PENDING).stream().findFirst().get();

        CartEvent purchase = edit("e-buy", "cart-race", "user-1");
        purchase.setActivityType(ActivityType.PURCHASE);
        module.cartEventProcessor().process(purchase);

        var outcome = module.notificationDispatcher().dispatch(reminder);
        assertThat(outcome.status()).isEqualTo(JobStatus.CANCELLED);
        assertThat(outcome.reason()).isEqualTo(CancellationReason.PURCHASED);
    }

    @Test
    void versionMismatchCancelsStaleReminderAtDispatch() {
        module.cartEventProcessor().process(edit("e1", "cart-stale", "user-1"));
        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);
        ScheduleJob reminder = jobs("cart-stale", JobType.REMINDER, JobStatus.PENDING).stream().findFirst().get();

        clock.advance(Duration.ofMinutes(11));
        module.cartEventProcessor().process(edit("e2", "cart-stale", "user-1"));

        var outcome = module.notificationDispatcher().dispatch(reminder);
        assertThat(outcome.status()).isEqualTo(JobStatus.CANCELLED);
        assertThat(outcome.reason()).isEqualTo(CancellationReason.VERSION_MISMATCH);
    }

    @Test
    void guestMergeCancelsGuestJourneyAndReschedulesAccountCart() {
        CartEvent guestEdit = edit("g1", "guest-cart", null);
        guestEdit.setUserType(UserType.SESSION);
        guestEdit.setSessionId("sess-1");
        module.cartEventProcessor().process(guestEdit);

        CartEvent merge = edit("m1", "guest-cart", "user-9");
        merge.setActivityType(ActivityType.MERGE);
        merge.setSessionId("sess-1");
        merge.setTargetCartId("account-cart");
        merge.setUserType(UserType.ACCOUNT);
        module.cartEventProcessor().process(merge);

        assertThat(module.cartActivityDao().findByCartId("guest-cart").orElseThrow().getState())
                .isEqualTo(CartState.CLEARED);
        assertThat(jobs("guest-cart", JobType.ABANDONMENT_CONFIRM, JobStatus.CANCELLED)).isNotEmpty();
        assertThat(module.cartActivityDao().findByCartId("account-cart")).isPresent();
        assertThat(jobs("account-cart", JobType.ABANDONMENT_CONFIRM, JobStatus.PENDING)).hasSize(1);
    }

    @Test
    void holdoutDoesNotScheduleReminders() {
        abService.reminderEnabled = false;
        module = new CartAbandonmentModule(clock, config, abService);
        module.cartEventProcessor().process(edit("e1", "cart-hold", "user-1"));
        clock.advance(Duration.ofMinutes(30));
        module.jobRunner().runDue(20);

        assertThat(jobs("cart-hold", JobType.ABANDONMENT_CONFIRM, JobStatus.FIRED)).hasSize(1);
        assertThat(jobs("cart-hold", JobType.REMINDER, JobStatus.PENDING)).isEmpty();
    }

    private List<ScheduleJob> jobs(String cartId, JobType type, JobStatus status) {
        return module.scheduler().jobsForCart(cartId).stream()
                .filter(job -> job.getJobType() == type && job.getStatus() == status)
                .toList();
    }

    private CartEvent edit(String eventId, String cartId, String userId) {
        CartEvent event = new CartEvent();
        event.setEventId(eventId);
        event.setCartId(cartId);
        event.setUserId(userId);
        event.setUserType(userId == null ? UserType.SESSION : UserType.ACCOUNT);
        event.setActivityType(ActivityType.EDIT);
        event.setActivityTime(clock.now());
        return event;
    }

    private static final class FixedAbService implements AbService {
        private boolean reminderEnabled;
        private final List<Integer> windows;
        private final String template;

        private FixedAbService(boolean reminderEnabled, List<Integer> windows, String template) {
            this.reminderEnabled = reminderEnabled;
            this.windows = windows;
            this.template = template;
        }

        @Override
        public boolean isEnabled(String experimentKey, String subjectId) {
            return true;
        }

        @Override
        public <T> T getValue(String experimentKey, String variable, String subjectId, T defaultValue) {
            return defaultValue;
        }

        @Override
        public CartReminderVariant getCartReminderVariant(String subjectId, CartReminderVariant defaults) {
            return new CartReminderVariant(windows, template, reminderEnabled);
        }
    }
}
