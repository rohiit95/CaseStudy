package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.ScheduleJob;

public interface JobHandler {

    void handle(ScheduleJob job);
}
