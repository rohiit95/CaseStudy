package com.casestudy.config;

import java.util.List;

public interface ConfigService {

    boolean isRemindersEnabled();

    List<Integer> getReminderWindowsInMinutes();

    String getMessageTemplate();

    /**
     * Inactivity window X before a cart is treated as abandoned (minutes).
     */
    int getAbandonmentWindowInMinutes();

    /**
     * Coalesce cart edits that arrive within this window instead of resetting
     * the abandonment timer on every event (minutes).
     */
    int getDebounceWindowInMinutes();
}
