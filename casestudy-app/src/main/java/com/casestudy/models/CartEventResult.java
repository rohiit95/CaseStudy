package com.casestudy.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartEventResult {

    private String cartId;
    private String userId;
    private Integer activityVersion;
    private LocalDateTime lastActivityTime;
    private CartActivityState state;
    private String lastEventId;
    private String cartActivityCol;
    private boolean duplicate;
    private List<ReminderSchedule> reminders = new ArrayList<>();

    public static CartEventResult from(CartActivity cart, List<ReminderSchedule> reminders, boolean duplicate) {
        CartEventResult result = new CartEventResult();
        result.cartId = cart.getCartId();
        result.userId = cart.getUserId();
        result.activityVersion = cart.getActivityVersion();
        result.lastActivityTime = cart.getLastActivityTime();
        result.state = cart.getState();
        result.lastEventId = cart.getLastEventId();
        result.cartActivityCol = cart.getCartActivityCol();
        result.duplicate = duplicate;
        result.reminders = reminders == null ? List.of() : reminders;
        return result;
    }

    public String getCartId() {
        return cartId;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getActivityVersion() {
        return activityVersion;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public CartActivityState getState() {
        return state;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public String getCartActivityCol() {
        return cartActivityCol;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public List<ReminderSchedule> getReminders() {
        return reminders;
    }
}
