package com.casestudy.abandonment.exception;

public final class CartNotFoundException extends CartAbandonmentException {

    public CartNotFoundException(String cartId) {
        super("Cart not found: " + cartId);
    }
}
