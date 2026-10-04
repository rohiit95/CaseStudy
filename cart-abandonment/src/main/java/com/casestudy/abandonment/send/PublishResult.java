package com.casestudy.abandonment.send;

public record PublishResult(boolean success, String providerMessageId, String detail) {

    public static PublishResult ok(String providerMessageId) {
        return new PublishResult(true, providerMessageId, "published");
    }

    public static PublishResult failed(String detail) {
        return new PublishResult(false, null, detail);
    }
}
