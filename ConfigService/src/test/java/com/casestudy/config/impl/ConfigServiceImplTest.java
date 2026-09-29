package com.casestudy.config.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServiceImplTest {

    private final ConfigServiceImpl configService = new ConfigServiceImpl();

    @Test
    void remindersAreEnabledByDefault() {
        assertThat(configService.isRemindersEnabled()).isTrue();
    }

    @Test
    void reminderWindowsMatchInactivitySchedule() {
        assertThat(configService.getReminderWindowsInMinutes()).containsExactly(30, 60, 1440);
    }

    @Test
    void defaultMessageTemplateIsSet() {
        assertThat(configService.getMessageTemplate()).isEqualTo("cart-abandoned-default");
    }
}
