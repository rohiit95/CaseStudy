package com.casestudy.pipeline.send;

public record PublishResult(boolean success, String providerMessageId, String detail) {

    public static PublishResult success(String providerMessageId) {
        return new PublishResult(true, providerMessageId, "published");
    }

    public static PublishResult failure(String detail) {
        return new PublishResult(false, null, detail);
    }
}
