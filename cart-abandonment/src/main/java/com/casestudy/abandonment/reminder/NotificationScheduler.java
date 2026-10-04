package com.casestudy.abandonment.reminder;

import com.casestudy.abandonment.model.CartActivity;

import java.util.List;

public interface NotificationScheduler {

    List<com.casestudy.abandonment.model.ScheduleJob> scheduleReminders(CartActivity cart);
}
