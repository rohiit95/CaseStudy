package com.casestudy.config.impl;

import com.casestudy.config.ConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    static final List<Integer> DEFAULT_REMINDER_WINDOWS_MINS = List.of(30, 60, 1440);

    @Override
    public boolean isRemindersEnabled() {
        return true;
    }

    @Override
    public List<Integer> getReminderWindowsInMinutes() {
        return DEFAULT_REMINDER_WINDOWS_MINS;
    }

    @Override
    public String getMessageTemplate() {
        return "cart-abandoned-default";
    }

    @Override
    public int getAbandonmentWindowInMinutes() {
        return 30;
    }

    @Override
    public int getDebounceWindowInMinutes() {
        return 10;
    }
}
