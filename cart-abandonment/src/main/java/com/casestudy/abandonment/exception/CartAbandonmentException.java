package com.casestudy.abandonment.exception;

public class CartAbandonmentException extends RuntimeException {

    public CartAbandonmentException(String message) {
        super(message);
    }

    public CartAbandonmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
