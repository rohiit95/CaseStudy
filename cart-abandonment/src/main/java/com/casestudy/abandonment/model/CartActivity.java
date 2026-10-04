package com.casestudy.abandonment.model;

import java.time.LocalDateTime;

public final class CartActivity {

    private String cartId;
    private String userId;
    private String sessionId;
    private int cartVersion;
    private LocalDateTime lastActivityTime;
    private CartState state;
    private String lastEventId;
    private UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public int getCartVersion() {
        return cartVersion;
    }

    public void setCartVersion(int cartVersion) {
        this.cartVersion = cartVersion;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public CartState getState() {
        return state;
    }

    public void setState(CartState state) {
        this.state = state;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
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

    public static CartActivity copyOf(CartActivity source) {
        if (source == null) {
            return null;
        }
        CartActivity copy = new CartActivity();
        copy.cartId = source.cartId;
        copy.userId = source.userId;
        copy.sessionId = source.sessionId;
        copy.cartVersion = source.cartVersion;
        copy.lastActivityTime = source.lastActivityTime;
        copy.state = source.state;
        copy.lastEventId = source.lastEventId;
        copy.userType = source.userType;
        copy.createdAt = source.createdAt;
        copy.updatedAt = source.updatedAt;
        return copy;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
