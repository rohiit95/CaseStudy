package com.casestudy.ab.impl;

import com.casestudy.ab.AbBucket;
import com.casestudy.ab.AbService;
import com.casestudy.ab.AbVariables;
import com.casestudy.ab.CartReminderVariant;
import com.casestudy.ab.ReminderChannel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class AbServiceImpl implements AbService {

    public static final String CART_REMINDERS_EXPERIMENT = "cart-reminders";

    static final CartReminderVariant TEST_1 = new CartReminderVariant(
            List.of(15, 45, 180),
            "cart-short-nudge",
            true,
            ReminderChannel.EMAIL
    );
    static final CartReminderVariant TEST_2 = new CartReminderVariant(
            List.of(20, 40, 120),
            "cart-mid-nudge",
            true,
            ReminderChannel.SMS
    );
    static final CartReminderVariant TEST_3 = new CartReminderVariant(
            List.of(10, 30, 90),
            "cart-urgent-nudge",
            true,
            ReminderChannel.PUSH
    );
    static final CartReminderVariant TEST_4 = new CartReminderVariant(
            List.of(60, 180, 1440),
            "cart-long-nudge",
            false,
            ReminderChannel.EMAIL
    );

    private static final Map<String, List<CartReminderVariant>> TEST_BUCKET_VARIANTS = Map.of(
            CART_REMINDERS_EXPERIMENT, List.of(TEST_1, TEST_2, TEST_3, TEST_4)
    );

    @Override
    public boolean isEnabled(String experimentKey, String subjectId) {
        return CART_REMINDERS_EXPERIMENT.equals(experimentKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String experimentKey, String variable, String subjectId, T defaultValue) {
        CartReminderVariant defaults = defaultsFor(variable, defaultValue);
        CartReminderVariant assigned = getCartReminderVariant(experimentKey, subjectId, defaults);
        Object value = switch (variable) {
            case AbVariables.REMINDER_WINDOWS -> assigned.reminderWindows();
            case AbVariables.MESSAGE_TEMPLATE -> assigned.messageTemplate();
            case AbVariables.REMINDER_ENABLED -> assigned.reminderEnabled();
            case AbVariables.CHANNEL -> assigned.channel();
            default -> defaultValue;
        };
        return (T) value;
    }

    @Override
    public CartReminderVariant getCartReminderVariant(String subjectId, CartReminderVariant defaults) {
        return getCartReminderVariant(CART_REMINDERS_EXPERIMENT, subjectId, defaults);
    }

    private CartReminderVariant getCartReminderVariant(
            String experimentKey,
            String subjectId,
            CartReminderVariant defaults
    ) {
        CartReminderVariant fallback = defaults == null
                ? new CartReminderVariant(List.of(), "", false, ReminderChannel.EMAIL)
                : defaults;
        AbBucket bucket = assignBucket(experimentKey, subjectId);
        if (bucket.isDefault()) {
            return fallback;
        }
        List<CartReminderVariant> testVariants = TEST_BUCKET_VARIANTS.get(experimentKey);
        if (testVariants == null) {
            return fallback;
        }
        return testVariants.get(bucket.ordinal() - 1);
    }

    AbBucket assignBucket(String experimentKey, String subjectId) {
        if (!StringUtils.hasText(subjectId) || !TEST_BUCKET_VARIANTS.containsKey(experimentKey)) {
            return AbBucket.DEFAULT;
        }
        int index = Math.floorMod((experimentKey + ":" + subjectId).hashCode(), AbBucket.COUNT);
        return AbBucket.fromIndex(index);
    }

    @SuppressWarnings("unchecked")
    private static <T> CartReminderVariant defaultsFor(String variable, T defaultValue) {
        return switch (variable) {
            case AbVariables.REMINDER_WINDOWS -> new CartReminderVariant(
                    defaultValue instanceof List<?> windows ? (List<Integer>) windows : List.of(),
                    "",
                    false,
                    ReminderChannel.EMAIL
            );
            case AbVariables.MESSAGE_TEMPLATE -> new CartReminderVariant(
                    List.of(),
                    defaultValue instanceof String template ? template : "",
                    false,
                    ReminderChannel.EMAIL
            );
            case AbVariables.REMINDER_ENABLED -> new CartReminderVariant(
                    List.of(),
                    "",
                    defaultValue instanceof Boolean enabled && enabled,
                    ReminderChannel.EMAIL
            );
            case AbVariables.CHANNEL -> new CartReminderVariant(
                    List.of(),
                    "",
                    false,
                    defaultValue instanceof ReminderChannel ch ? ch : ReminderChannel.EMAIL
            );
            default -> new CartReminderVariant(List.of(), "", false, ReminderChannel.EMAIL);
        };
    }
}
