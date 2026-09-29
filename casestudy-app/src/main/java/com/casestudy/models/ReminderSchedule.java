package com.casestudy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.casestudy.time.DateTimes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ReminderSchedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_version_window",
                columnNames = {"cartId", "activityVersion", "reminderWindowInMins"}
        ),
        indexes = {
                @Index(name = "idx_status_scheduledAt", columnList = "status, scheduledAt"),
                @Index(name = "idx_cartId_status", columnList = "cartId, status")
        }
)
public class ReminderSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminderId")
    private Long reminderId;

    @Column(name = "cartId", nullable = false, length = 64)
    private String cartId;

    @Column(name = "activityVersion", nullable = false)
    private Integer activityVersion;

    @Column(name = "reminderWindowInMins", nullable = false)
    private Integer reminderWindowInMins;

    @Column(name = "messageTemplate", length = 64)
    private String messageTemplate;

    @Column(name = "scheduledAt", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "attemptCount", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = DateTimes.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = ReminderStatus.PENDING;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = DateTimes.now();
    }

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public Integer getActivityVersion() {
        return activityVersion;
    }

    public void setActivityVersion(Integer activityVersion) {
        this.activityVersion = activityVersion;
    }

    public Integer getReminderWindowInMins() {
        return reminderWindowInMins;
    }

    public void setReminderWindowInMins(Integer reminderWindowInMins) {
        this.reminderWindowInMins = reminderWindowInMins;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
