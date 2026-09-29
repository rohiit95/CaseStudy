package com.casestudy.ab;

import java.util.List;

public record CartReminderVariant(
        List<Integer> reminderWindows,
        String messageTemplate,
        boolean reminderEnabled
) {
    public CartReminderVariant {
        reminderWindows = reminderWindows == null ? List.of() : List.copyOf(reminderWindows);
        messageTemplate = messageTemplate == null ? "" : messageTemplate;
    }
}
