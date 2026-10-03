package com.casestudy.dao.redis;

import com.casestudy.models.ReminderSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Optional index for due reminders (Redis in production; in-memory simulation here).
 */
public interface DueReminderIndex {

    void register(ReminderSchedule reminder);

    void removeCart(String cartId);

    List<Long> pollDueIds(int limit, LocalDateTime now);
}
