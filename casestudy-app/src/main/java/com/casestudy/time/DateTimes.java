package com.casestudy.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimes {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private DateTimes() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(IST);
    }
}
