package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.ScheduleJob;

public interface NotificationDispatcher {

    DispatchResult dispatch(ScheduleJob job);
}
