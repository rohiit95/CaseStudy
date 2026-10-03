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
        int updated = entityManager.createQuery("""
                        update ReminderSchedule r
                        set r.status = :cancelled,
                            r.updatedAt = :now
                        where r.cartId = :cartId
                          and r.status = :pending
                        """)
                .setParameter("cancelled", ReminderStatus.CANCELLED)
                .setParameter("now", now)
                .setParameter("cartId", cartId)
                .setParameter("pending", ReminderStatus.PENDING)
                .executeUpdate();
        entityManager.clear();
        return updated;
    }

    @Override
    public List<ReminderSchedule> saveAll(List<ReminderSchedule> reminders) {
        List<ReminderSchedule> saved = new ArrayList<>();
        for (ReminderSchedule reminder : reminders) {
            saved.add(entityManager.merge(reminder));
        }
        entityManager.flush();
        return saved;
    }

    @Override
    public List<ReminderSchedule> findByCartIdAndActivityVersion(String cartId, Integer activityVersion) {
        return entityManager.createQuery("""
                        select r from ReminderSchedule r
                        where r.cartId = :cartId
                          and r.activityVersion = :activityVersion
                        order by r.reminderWindowInMins asc
                        """, ReminderSchedule.class)
                .setParameter("cartId", cartId)
                .setParameter("activityVersion", activityVersion)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReminderSchedule> claimDuePending(int limit, LocalDateTime now) {
        List<ReminderSchedule> locked = entityManager.createNativeQuery(
                """
                SELECT * FROM ReminderSchedule
                WHERE status = 'PENDING' AND scheduledAt <= :now
                LIMIT :limit FOR UPDATE SKIP LOCKED
                """,
                ReminderSchedule.class
        )
                .setParameter("now", now)
                .setParameter("limit", limit)
                .getResultList();

        for (ReminderSchedule reminder : locked) {
            reminder.setStatus(ReminderStatus.CLAIMED);
            reminder.setUpdatedAt(now);
        }
        entityManager.flush();
        return locked;
    }

    @Override
    public Optional<ReminderSchedule> findById(Long reminderId) {
        return Optional.ofNullable(entityManager.find(ReminderSchedule.class, reminderId));
    }

    @Override
    public void updateStatus(ReminderSchedule reminder, ReminderStatus status, LocalDateTime now) {
        ReminderSchedule managed = entityManager.merge(reminder);
        managed.setStatus(status);
        managed.setUpdatedAt(now);
        entityManager.flush();
    }

    @Override
    public void markProcessed(ReminderSchedule reminder, LocalDateTime now) {
        ReminderSchedule managed = entityManager.merge(reminder);
        managed.setStatus(ReminderStatus.FIRED);
        managed.setUpdatedAt(now);
        entityManager.flush();
    }

    @Override
    public void markFailed(ReminderSchedule reminder, LocalDateTime now) {
        ReminderSchedule managed = entityManager.merge(reminder);
        managed.setAttemptCount(managed.getAttemptCount() + 1);
        managed.setStatus(ReminderStatus.FAILED);
        managed.setUpdatedAt(now);
        entityManager.flush();
    }
}
