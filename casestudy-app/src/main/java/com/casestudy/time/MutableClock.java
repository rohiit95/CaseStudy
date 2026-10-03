package com.casestudy.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public final class MutableClock extends Clock {

    private volatile Instant instant;
    private final ZoneId zone;

    public MutableClock(ZoneId zone) {
        this.zone = zone;
        this.instant = Instant.now();
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
