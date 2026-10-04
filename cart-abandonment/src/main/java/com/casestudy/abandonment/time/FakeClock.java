package com.casestudy.abandonment.time;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public final class FakeClock implements Clock {

    private Instant instant;
    private final ZoneId zone;

    public FakeClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public static FakeClock startedAt(Instant instant) {
        return new FakeClock(instant, ZoneId.of("Asia/Kolkata"));
    }

    public void advance(Duration duration) {
        instant = instant.plus(duration);
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public Instant instant() {
        return instant;
    }

    @Override
    public ZoneId zone() {
        return zone;
    }
}
