package com.casestudy.dao.impl;

import com.casestudy.dao.ReminderScheduleDao;
import com.casestudy.models.ReminderSchedule;
import com.casestudy.models.ReminderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReminderScheduleDaoImpl implements ReminderScheduleDao {

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
}
