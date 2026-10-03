package com.casestudy.dao.memory;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.time.DateTimes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Repository
@ConditionalOnProperty(prefix = "casestudy", name = "storage", havingValue = "in-memory")
public class InMemoryReminderScheduleStore implements ReminderScheduleStore {

    private final AtomicLong idSequence = new AtomicLong(1);
    private final Map<Long, ReminderSchedule> byId = new ConcurrentHashMap<>();
    private final ReentrantLock claimLock = new ReentrantLock();

    @Override
    public int cancelPendingByCartId(String cartId, LocalDateTime now) {
        int cancelled = 0;
        for (ReminderSchedule reminder : byId.values()) {
            if (cartId.equals(reminder.getCartId()) && reminder.getStatus() == ReminderStatus.PENDING) {
                reminder.setStatus(ReminderStatus.CANCELLED);
                reminder.setUpdatedAt(now);
                cancelled++;
            }
        }
        return cancelled;
    }

    @Override
    public List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders) {
        List<ReminderSchedule> saved = new ArrayList<>();
        for (ReminderSchedule reminder : reminders) {
            ReminderSchedule copy = copy(reminder);
            if (copy.getReminderId() == null) {
                copy.setReminderId(idSequence.getAndIncrement());
            }
            if (copy.getCreatedAt() == null) {
                copy.setCreatedAt(DateTimes.now());
            }
            copy.setUpdatedAt(DateTimes.now());
            byId.put(copy.getReminderId(), copy);
            saved.add(copy(copy));
        }
        return saved;
    }

    @Override
    public List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion) {
        return byId.values().stream()
                .filter(r -> cartId.equals(r.getCartId()) && activityVersion.equals(r.getActivityVersion()))
                .sorted(Comparator.comparing(ReminderSchedule::getReminderWindowInMins))
                .map(InMemoryReminderScheduleStore::copy)
                .toList();
    }

    @Override
    public List<ReminderSchedule> claimDuePending(int limit, LocalDateTime now) {
        claimLock.lock();
        try {
            List<ReminderSchedule> due = byId.values().stream()
                    .filter(r -> r.getStatus() == ReminderStatus.PENDING)
                    .filter(r -> !r.getScheduledAt().isAfter(now))
                    .sorted(Comparator.comparing(ReminderSchedule::getScheduledAt))
                    .limit(limit)
                    .toList();
            List<ReminderSchedule> claimed = new ArrayList<>();
            for (ReminderSchedule reminder : due) {
                reminder.setStatus(ReminderStatus.CLAIMED);
                reminder.setUpdatedAt(now);
                claimed.add(copy(reminder));
            }
            return claimed;
        } finally {
            claimLock.unlock();
        }
    }

    @Override
    public Optional<ReminderSchedule> findById(Long reminderId) {
        return Optional.ofNullable(byId.get(reminderId)).map(InMemoryReminderScheduleStore::copy);
    }

    @Override
    public void updateStatus(ReminderSchedule reminder, ReminderStatus status, LocalDateTime now) {
        ReminderSchedule stored = byId.get(reminder.getReminderId());
        if (stored != null) {
            stored.setStatus(status);
            stored.setUpdatedAt(now);
        }
    }

    @Override
    public void markProcessed(ReminderSchedule reminder, LocalDateTime now) {
        ReminderSchedule stored = requireStored(reminder);
        stored.setStatus(ReminderStatus.FIRED);
        stored.setUpdatedAt(now);
    }

    @Override
    public void markFailed(ReminderSchedule reminder, LocalDateTime now) {
        ReminderSchedule stored = requireStored(reminder);
        stored.setAttemptCount(stored.getAttemptCount() + 1);
        stored.setStatus(ReminderStatus.FAILED);
        stored.setUpdatedAt(now);
    }

    private ReminderSchedule requireStored(ReminderSchedule reminder) {
        ReminderSchedule stored = byId.get(reminder.getReminderId());
        if (stored == null) {
            throw new IllegalStateException("Reminder not found: " + reminder.getReminderId());
        }
        return stored;
    }

    private static ReminderSchedule copy(ReminderSchedule source) {
        ReminderSchedule copy = new ReminderSchedule();
        copy.setReminderId(source.getReminderId());
        copy.setCartId(source.getCartId());
        copy.setActivityVersion(source.getActivityVersion());
        copy.setReminderWindowInMins(source.getReminderWindowInMins());
        copy.setMessageTemplate(source.getMessageTemplate());
        copy.setScheduledAt(source.getScheduledAt());
        copy.setStatus(source.getStatus());
        copy.setAttemptCount(source.getAttemptCount());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }
}
