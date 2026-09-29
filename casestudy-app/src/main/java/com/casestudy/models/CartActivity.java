package com.casestudy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.casestudy.time.DateTimes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cartactivity",
        indexes = {
                @Index(name = "idx_userId", columnList = "userId"),
                @Index(name = "idx_state_lastActivity", columnList = "state, lastActivityTime"),
                @Index(name = "idx_cartId_lastEventId", columnList = "lastEventId, cartId")
        }
)
public class CartActivity {

    @Id
    @Column(name = "cartId", nullable = false, length = 64)
    private String cartId;

    @Column(name = "userId", length = 64)
    private String userId;

    @Column(name = "activityVersion", nullable = false)
    private Integer activityVersion = 1;

    @Column(name = "lastActivityTime", nullable = false)
    private LocalDateTime lastActivityTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private CartActivityState state = CartActivityState.ACTIVE;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "lastEventId", nullable = false, length = 45)
    private String lastEventId;

    @Column(name = "cartactivitycol", nullable = false, length = 45)
    private String cartActivityCol;

    @PrePersist
    void onCreate() {
        LocalDateTime now = DateTimes.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (activityVersion == null) {
            activityVersion = 1;
        }
        if (state == null) {
            state = CartActivityState.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = DateTimes.now();
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getActivityVersion() {
        return activityVersion;
    }

    public void setActivityVersion(Integer activityVersion) {
        this.activityVersion = activityVersion;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public CartActivityState getState() {
        return state;
    }

    public void setState(CartActivityState state) {
        this.state = state;
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

    public String getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    public String getCartActivityCol() {
        return cartActivityCol;
    }

    public void setCartActivityCol(String cartActivityCol) {
        this.cartActivityCol = cartActivityCol;
    }
}
