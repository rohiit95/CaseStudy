package com.casestudy.config;

import com.casestudy.time.DateTimes;
import com.casestudy.time.MutableClock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class ApplicationConfiguration {

    @Bean
    public MutableClock mutableClock() {
        MutableClock clock = new MutableClock(DateTimes.IST);
        DateTimes.useClock(clock);
        return clock;
    }

    @Bean
    public Clock clock(MutableClock mutableClock) {
        return mutableClock;
    }
}
