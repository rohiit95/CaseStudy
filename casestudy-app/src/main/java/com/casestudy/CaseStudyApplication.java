package com.casestudy;

import com.casestudy.time.DateTimes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.casestudy")
public class CaseStudyApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimes.IST));
        SpringApplication.run(CaseStudyApplication.class, args);
    }
}
