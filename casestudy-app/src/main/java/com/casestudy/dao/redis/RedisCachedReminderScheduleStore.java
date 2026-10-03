package com.casestudy.dao.redis;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.dao.mysql.JpaReminderScheduleStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@Transactional
@ConditionalOnProperty(prefix = "casestudy", name = "storage", havingValue = "mysql-redis")
public class RedisCachedReminderScheduleStore implements ReminderScheduleStore {

    private final JpaReminderScheduleStore delegate;
    private final DueReminderIndex dueReminderIndex;

    public RedisCachedReminderScheduleStore(
            JpaReminderScheduleStore delegate,
            DueReminderIndex dueReminderIndex
    ) {
        this.delegate = delegate;
        this.dueReminderIndex = dueReminderIndex;
    }

    @Override
    public int cancelPendingByCartId(String cartId, LocalDateTime now) {
        dueReminderIndex.removeCart(cartId);
        return delegate.cancelPendingByCartId(cartId, now);
    }

    @Override
    public List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders) {
        List<ReminderSchedule> saved = delegate.saveAll(reminders);
        saved.forEach(dueReminderIndex::register);
        return saved;
    }

    @Override
    public List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion) {
        return delegate.findByCartIdAndActivityVersion(cartId, activityVersion);
    }

    @Override
    public List<ReminderSchedule> claimDuePending(int limit, LocalDateTime now) {
        List<Long> hintedIds = dueReminderIndex.pollDueIds(limit, now);
        if (hintedIds.isEmpty()) {
            return delegate.claimDuePending(limit, now);
        }
        return delegate.claimDuePending(limit, now);
    }

    @Override
    public Optional<ReminderSchedule> findById(Long reminderId) {
        return delegate.findById(reminderId);
    }

    @Override
    public void updateStatus(ReminderSchedule reminder, ReminderStatus status, LocalDateTime now) {
        delegate.updateStatus(reminder, status, now);
    }

    @Override
    public void markProcessed(ReminderSchedule reminder, LocalDateTime now) {
        delegate.markProcessed(reminder, now);
    }

    @Override
    public void markFailed(ReminderSchedule reminder, LocalDateTime now) {
        delegate.markFailed(reminder, now);
    }
}
