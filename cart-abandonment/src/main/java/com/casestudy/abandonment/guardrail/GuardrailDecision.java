package com.casestudy.abandonment.guardrail;

import com.casestudy.abandonment.model.CancellationReason;

public record GuardrailDecision(boolean accepted, CancellationReason suppressionReason) {

    public static GuardrailDecision allow() {
        return new GuardrailDecision(true, null);
    }

    public static GuardrailDecision deny(CancellationReason reason) {
        return new GuardrailDecision(false, reason);
    }
}
