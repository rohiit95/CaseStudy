package com.casestudy.abandonment.scheduler;

public final class ScheduleKeys {

    private ScheduleKeys() {
    }

    public static String abandonment(String cartId, int version) {
        return cartId + ":" + version + ":abandonment";
    }

    public static String reminder(String cartId, int version, int windowMinutes) {
        return reminderPrefix(cartId, version) + windowMinutes;
    }

    private static String reminderPrefix(String cartId, int version) {
        return cartId + ":" + version + ":reminder:";
    }
}
