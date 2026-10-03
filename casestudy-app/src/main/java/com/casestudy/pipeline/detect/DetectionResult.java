package com.casestudy.pipeline.detect;

import com.casestudy.models.CartActivity;

public record DetectionResult(CartActivity cart, boolean duplicate, boolean newlyCreated) {

    public static DetectionResult duplicate(CartActivity cart) {
        return new DetectionResult(cart, true, false);
    }

    public static DetectionResult updated(CartActivity cart, boolean newlyCreated) {
        return new DetectionResult(cart, false, newlyCreated);
    }
}
