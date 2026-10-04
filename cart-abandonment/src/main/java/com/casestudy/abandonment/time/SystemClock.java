package com.casestudy.abandonment.time;

import java.time.Instant;
import java.time.ZoneId;

public final class SystemClock implements Clock {

    private final ZoneId zone;

    public SystemClock(ZoneId zone) {
        this.zone = zone;
    }

    @Override
    public Instant instant() {
        return Instant.now();
    }

    @Override
    public ZoneId zone() {
        return zone;
    }
}
