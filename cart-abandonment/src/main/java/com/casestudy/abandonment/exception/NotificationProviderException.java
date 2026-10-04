package com.casestudy.abandonment.exception;

public class NotificationProviderException extends CartAbandonmentException {

    public NotificationProviderException(String message) {
        super(message);
    }

    public NotificationProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean retryable() {
        return true;
    }
}
