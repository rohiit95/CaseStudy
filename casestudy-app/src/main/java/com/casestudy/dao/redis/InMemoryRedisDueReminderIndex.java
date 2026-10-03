package com.casestudy.dao.redis;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates a Redis sorted set of due reminder IDs. Swap for RedisTemplate in production.
 */
@Component
@ConditionalOnProperty(prefix = "casestudy", name = "storage", havingValue = "mysql-redis")
public class InMemoryRedisDueReminderIndex implements DueReminderIndex {

    private final Map<Long, ReminderSchedule> index = new ConcurrentHashMap<>();

    @Override
    public void register(ReminderSchedule reminder) {
        if (reminder.getReminderId() != null) {
            index.put(reminder.getReminderId(), reminder);
        }
    }

    @Override
    public void removeCart(String cartId) {
        index.entrySet().removeIf(e -> cartId.equals(e.getValue().getCartId()));
    }

    @Override
    public List<Long> pollDueIds(int limit, LocalDateTime now) {
        List<Long> ids = index.values().stream()
                .filter(r -> r.getStatus() == ReminderStatus.PENDING)
                .filter(r -> !r.getScheduledAt().isAfter(now))
                .sorted(Comparator.comparing(ReminderSchedule::getScheduledAt))
                .limit(limit)
                .map(ReminderSchedule::getReminderId)
                .toList();
        return new ArrayList<>(ids);
    }
}
