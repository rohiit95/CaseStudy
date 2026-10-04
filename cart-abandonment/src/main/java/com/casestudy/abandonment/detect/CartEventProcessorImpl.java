package com.casestudy.abandonment.detect;

import com.casestudy.abandonment.dao.CartActivityDao;
import com.casestudy.abandonment.model.ActivityType;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.CartActivity;
import com.casestudy.abandonment.model.CartEvent;
import com.casestudy.abandonment.model.CartProcessResult;
import com.casestudy.abandonment.model.CartState;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ProcessResultType;
import com.casestudy.abandonment.model.ScheduleMetadata;
import com.casestudy.abandonment.scheduler.Scheduler;
import com.casestudy.abandonment.time.Clock;
import com.casestudy.config.ConfigService;

import java.time.LocalDateTime;
import java.util.Optional;

public final class CartEventProcessorImpl implements CartEventProcessor {

    private final CartActivityDao cartActivityDao;
    private final Scheduler scheduler;
    private final ConfigService configService;
    private final Clock clock;

    public CartEventProcessorImpl(
            CartActivityDao cartActivityDao,
            Scheduler scheduler,
            ConfigService configService,
            Clock clock
    ) {
        this.cartActivityDao = cartActivityDao;
        this.scheduler = scheduler;
        this.configService = configService;
        this.clock = clock;
    }

    @Override
    public CartProcessResult process(CartEvent event) {
        LocalDateTime activityTime = event.getActivityTime() != null ? event.getActivityTime() : clock.now();
        Optional<CartActivity> existing = cartActivityDao.findByCartId(event.getCartId());

        if (existing.isPresent() && event.getEventId().equals(existing.get().getLastEventId())) {
            return new CartProcessResult(existing.get(), ProcessResultType.DUPLICATE);
        }
        if (existing.isPresent()
                && existing.get().getLastActivityTime() != null
                && activityTime.isBefore(existing.get().getLastActivityTime())) {
            return new CartProcessResult(existing.get(), ProcessResultType.IGNORED_OUT_OF_ORDER);
        }

        if (event.getActivityType() == ActivityType.MERGE) {
            return merge(event, activityTime);
        }
        if (event.getActivityType() == ActivityType.CLEAR || event.getActivityType() == ActivityType.PURCHASE) {
            return terminate(event, existing.orElseGet(CartActivity::new), activityTime);
        }
        return upsertActive(event, existing, activityTime);
    }

    private CartProcessResult upsertActive(
            CartEvent event,
            Optional<CartActivity> existing,
            LocalDateTime activityTime
    ) {
        boolean isNew = existing.isEmpty();
        CartActivity cart = existing.orElseGet(CartActivity::new);
        boolean withinDebounce = !isNew
                && cart.getLastActivityTime() != null
                && !activityTime.isAfter(cart.getLastActivityTime().plusMinutes(configService.getDebounceWindowInMinutes()))
                && cart.getState() == CartState.ACTIVE;

        if (isNew) {
            cart.setCartId(event.getCartId());
            cart.setCartVersion(1);
            cart.setCreatedAt(activityTime);
            cart.setState(CartState.ACTIVE);
        } else if (!withinDebounce) {
            scheduler.cancelPendingByCartId(cart.getCartId(), CancellationReason.ACTIVITY_RESUMED);
            cart.setCartVersion(cart.getCartVersion() + 1);
            cart.setState(CartState.ACTIVE);
        }

        applyIdentity(cart, event);
        cart.setLastActivityTime(activityTime);
        cart.setLastEventId(event.getEventId());
        cart.setUpdatedAt(activityTime);
        cart = cartActivityDao.save(cart);
        scheduleAbandonmentCheck(cart);
        return new CartProcessResult(cart, isNew ? ProcessResultType.CREATED : ProcessResultType.UPDATED);
    }

    private CartProcessResult terminate(CartEvent event, CartActivity cart, LocalDateTime activityTime) {
        if (cart.getCartId() == null) {
            cart.setCartId(event.getCartId());
            cart.setCartVersion(1);
            cart.setCreatedAt(activityTime);
        } else {
            cart.setCartVersion(cart.getCartVersion() + 1);
        }
        applyIdentity(cart, event);
        cart.setLastActivityTime(activityTime);
        cart.setLastEventId(event.getEventId());
        cart.setState(event.getActivityType() == ActivityType.PURCHASE ? CartState.PURCHASED : CartState.CLEARED);
        cart.setUpdatedAt(activityTime);
        CancellationReason reason = cart.getState() == CartState.PURCHASED
                ? CancellationReason.PURCHASED
                : CancellationReason.CLEARED;
        scheduler.cancelPendingByCartId(cart.getCartId(), reason);
        return new CartProcessResult(cartActivityDao.save(cart), ProcessResultType.TERMINATED);
    }

    private CartProcessResult merge(CartEvent event, LocalDateTime activityTime) {
        CartActivity guest = cartActivityDao.findByCartId(event.getCartId()).orElseGet(CartActivity::new);
        if (guest.getCartId() == null) {
            guest.setCartId(event.getCartId());
            guest.setCartVersion(1);
            guest.setCreatedAt(activityTime);
        } else {
            guest.setCartVersion(guest.getCartVersion() + 1);
        }
        guest.setState(CartState.CLEARED);
        guest.setLastEventId(event.getEventId());
        guest.setLastActivityTime(activityTime);
        guest.setUpdatedAt(activityTime);
        scheduler.cancelPendingByCartId(guest.getCartId(), CancellationReason.MERGED);
        cartActivityDao.save(guest);

        String targetCartId = event.getTargetCartId() != null ? event.getTargetCartId() : event.getUserId();
        CartEvent targetEvent = new CartEvent();
        targetEvent.setEventId(event.getEventId() + ":target");
        targetEvent.setCartId(targetCartId);
        targetEvent.setUserId(event.getUserId());
        targetEvent.setSessionId(event.getSessionId());
        targetEvent.setUserType(com.casestudy.abandonment.model.UserType.ACCOUNT);
        targetEvent.setActivityType(ActivityType.EDIT);
        targetEvent.setActivityTime(activityTime);
        upsertActive(targetEvent, cartActivityDao.findByCartId(targetCartId), activityTime);
        return new CartProcessResult(cartActivityDao.findByCartId(guest.getCartId()).orElse(guest), ProcessResultType.MERGED);
    }

    private void scheduleAbandonmentCheck(CartActivity cart) {
        String key = abandonmentKey(cart.getCartId(), cart.getCartVersion());
        LocalDateTime fireAt = cart.getLastActivityTime().plusMinutes(configService.getAbandonmentWindowInMinutes());
        ScheduleMetadata metadata = new ScheduleMetadata(
                cart.getCartId(),
                cart.getUserId(),
                cart.getSessionId(),
                cart.getUserType(),
                cart.getCartVersion(),
                null
        );
        scheduler.schedule(JobType.ABANDONMENT_CONFIRM, key, fireAt, metadata);
    }

    private String abandonmentKey(String cartId, int version) {
        return cartId + ":" + version + ":abandonment";
    }
    private static void applyIdentity(CartActivity cart, CartEvent event) {
        cart.setUserId(event.getUserId());
        cart.setSessionId(event.getSessionId());
        cart.setUserType(event.getUserType());
    }
}
