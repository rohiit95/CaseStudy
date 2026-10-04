package com.casestudy.abandonment.model;

import java.time.LocalDateTime;

public final class CartEvent {

    private String eventId;
    private String cartId;
    private String userId;
    private String sessionId;
    private UserType userType;
    private ActivityType activityType;
    private LocalDateTime activityTime;
    private String targetCartId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }

    public String getTargetCartId() {
        return targetCartId;
    }

    public void setTargetCartId(String targetCartId) {
        this.targetCartId = targetCartId;
    }

    public String subjectId() {
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        return cartId;
    }
}
