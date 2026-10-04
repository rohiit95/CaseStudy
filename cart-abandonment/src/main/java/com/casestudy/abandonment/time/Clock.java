package com.casestudy.abandonment.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface Clock {

    Instant instant();

    ZoneId zone();

    default LocalDateTime now() {
        return LocalDateTime.ofInstant(instant(), zone());
    }
}
