package com.casestudy.abandonment.guardrail;

import com.casestudy.abandonment.dao.CartActivityDao;
import com.casestudy.abandonment.model.CancellationReason;
import com.casestudy.abandonment.model.CartActivity;
import com.casestudy.abandonment.model.CartState;
import com.casestudy.abandonment.model.ScheduleJob;

public final class GuardrailEngineImpl implements GuardrailEngine {

    private final CartActivityDao cartActivityDao;

    public GuardrailEngineImpl(CartActivityDao cartActivityDao) {
        this.cartActivityDao = cartActivityDao;
    }

    @Override
    public GuardrailDecision evaluate(ScheduleJob job) {
        CartActivity cart = cartActivityDao.findByCartId(job.getMetadata().cartId()).orElse(null);
        if (cart == null) {
            return GuardrailDecision.deny(CancellationReason.VERSION_MISMATCH);
        }
        if (cart.getState() == CartState.PURCHASED) {
            return GuardrailDecision.deny(CancellationReason.PURCHASED);
        }
        if (cart.getState() == CartState.CLEARED) {
            return GuardrailDecision.deny(CancellationReason.CLEARED);
        }
        if (cart.getCartVersion() != job.getMetadata().cartVersion()) {
            return GuardrailDecision.deny(CancellationReason.VERSION_MISMATCH);
        }
        return GuardrailDecision.allow();
    }
}
