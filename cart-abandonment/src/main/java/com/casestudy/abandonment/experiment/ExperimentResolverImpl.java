package com.casestudy.abandonment.experiment;

import com.casestudy.ab.AbService;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.ab.ReminderChannel;
import com.casestudy.config.ConfigService;

public final class ExperimentResolverImpl implements ExperimentResolver {

    private final AbService abService;
    private final ConfigService configService;

    public ExperimentResolverImpl(AbService abService, ConfigService configService) {
        this.abService = abService;
        this.configService = configService;
    }

    @Override
    public CartReminderVariant resolve(String subjectId) {
        return abService.getCartReminderVariant(subjectId, defaults());
    }

    private CartReminderVariant defaults() {
        return new CartReminderVariant(
                configService.getReminderWindowsInMinutes(),
                configService.getMessageTemplate(),
                configService.isRemindersEnabled(),
                parseChannel(configService.getReminderChannel())
        );
    }

    private static ReminderChannel parseChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return ReminderChannel.EMAIL;
        }
        try {
            return ReminderChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ReminderChannel.EMAIL;
        }
    }
}
