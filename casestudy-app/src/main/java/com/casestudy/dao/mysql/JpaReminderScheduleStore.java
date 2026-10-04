package com.casestudy.dao.mysql;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import com.casestudy.dao.ReminderScheduleStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@ConditionalOnExpression("'${casestudy.storage:in-memory}' == 'mysql' || '${casestudy.storage:in-memory}' == 'mysql-redis'")
public class JpaReminderScheduleStore implements ReminderScheduleStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int cancelPendingByCartId(String cartId, LocalDateTime now) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReminderSchedule> claimDuePending(int limit, LocalDateTime now) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Optional<ReminderSchedule> findById(Long reminderId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void updateStatus(ReminderSchedule reminder, ReminderStatus status, LocalDateTime now) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void markProcessed(ReminderSchedule reminder, LocalDateTime now) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void markFailed(ReminderSchedule reminder, LocalDateTime now) {
        throw new RuntimeException("Not implemented");
    }
}
