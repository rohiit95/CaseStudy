package com.casestudy.dao;

import com.casestudy.models.ReminderSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderScheduleDao {

    int cancelPendingByCartId(String cartId, LocalDateTime now);

    List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders);

    List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion);
}
