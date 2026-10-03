package com.casestudy.dao;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderScheduleStore {

    int cancelPendingByCartId(String cartId, LocalDateTime now);

    List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders);

    List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion);

    /**
     * Claims due PENDING jobs (SKIP LOCKED semantics on MySQL).
     */
    List<ReminderSchedule> claimDuePending(int limit, LocalDateTime now);

    Optional<ReminderSchedule> findById(Long reminderId);

    void updateStatus(ReminderSchedule reminder, ReminderStatus status, LocalDateTime now);

    /**
     * Mark FIRED only after publish succeeds.
     */
    void markProcessed(ReminderSchedule reminder, LocalDateTime now);

    void markFailed(ReminderSchedule reminder, LocalDateTime now);
}
