package com.casestudy.pipeline.detect;

import com.casestudy.models.ActivityType;
import com.casestudy.models.CartActivity;
import com.casestudy.models.CartActivityState;
import com.casestudy.models.CartEvent;
import com.casestudy.dao.CartActivityStore;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.time.DateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AbandonmentDetector {

    private final CartActivityStore cartActivityStore;
    private final ReminderScheduleStore reminderScheduleStore;

    public AbandonmentDetector(
            CartActivityStore cartActivityStore,
            ReminderScheduleStore reminderScheduleStore
    ) {
        this.cartActivityStore = cartActivityStore;
        this.reminderScheduleStore = reminderScheduleStore;
    }

    @Transactional
    public DetectionResult detect(CartEvent event) {
        Optional<CartActivity> existing = cartActivityStore.findByCartId(event.getCartId());
        if (existing.isPresent() && event.getEventId().equals(existing.get().getLastEventId())) {
            return DetectionResult.duplicate(existing.get());
        }

        LocalDateTime activityTime = event.getActivityTime() != null
                ? event.getActivityTime()
                : DateTimes.now();

        CartActivity cart = existing.orElseGet(CartActivity::new);
        boolean isNew = cart.getCartId() == null;

        if (isNew) {
            cart.setCartId(event.getCartId());
            cart.setActivityVersion(1);
        } else {
            reminderScheduleStore.cancelPendingByCartId(cart.getCartId(), activityTime);
            cart.setActivityVersion(cart.getActivityVersion() + 1);
        }

        cart.setUserId(event.getUserId());
        cart.setLastActivityTime(activityTime);
        cart.setLastEventId(event.getEventId());
        cart.setCartActivityCol(event.getUserType().name());
        if (event.getActivityType() == ActivityType.CLEAR) {
            cart.setState(CartActivityState.CLEARED);
        } else if (event.getActivityType() == ActivityType.PURCHASE) {
            cart.setState(CartActivityState.PURCHASED);
        } else {
            cart.setState(CartActivityState.ACTIVE);
        }

        if (cart.getCreatedAt() == null) {
            cart.setCreatedAt(activityTime);
        }
        cart.setUpdatedAt(activityTime);

        cart = cartActivityStore.save(cart);
        return DetectionResult.updated(cart, isNew);
    }
}
