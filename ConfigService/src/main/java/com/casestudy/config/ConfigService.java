package com.casestudy.config;

import java.util.List;

public interface ConfigService {

    boolean isRemindersEnabled();

    List<Integer> getReminderWindowsInMinutes();

    String getMessageTemplate();
}
