package com.casestudy.pipeline.dispatch;

import com.casestudy.models.CartActivity;
import com.casestudy.models.CartActivityState;
import com.casestudy.models.ReminderSchedule;
import com.casestudy.dao.CartActivityStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FireTimeGuard {

    private final CartActivityStore cartActivityStore;

    public FireTimeGuard(CartActivityStore cartActivityStore) {
        this.cartActivityStore = cartActivityStore;
    }

    public SuppressionReason evaluate(ReminderSchedule job) {
        Optional<CartActivity> cart = cartActivityStore.findByCartId(job.getCartId());
        if (cart.isEmpty()) {
            return SuppressionReason.CART_MISSING;
        }
        CartActivity activity = cart.get();
        if (activity.getState() == CartActivityState.PURCHASED
                || activity.getState() == CartActivityState.CLEARED) {
            return SuppressionReason.CART_TERMINATED;
        }
        if (!activity.getActivityVersion().equals(job.getActivityVersion())) {
            return SuppressionReason.STALE_VERSION;
        }
        return SuppressionReason.NONE;
    }

    public enum SuppressionReason {
        NONE,
        CART_MISSING,
        CART_TERMINATED,
        STALE_VERSION
    }
}
