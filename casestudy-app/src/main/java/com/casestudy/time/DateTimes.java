package com.casestudy.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimes {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static volatile Clock clock = Clock.system(IST);

    private DateTimes() {
    }

    public static void useClock(Clock newClock) {
        clock = newClock;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public static Instant instant() {
        return clock.instant();
    }
}
