package com.casestudy.pipeline.dispatch;

import com.casestudy.models.ReminderSchedule;
import com.casestudy.dao.ReminderScheduleStore;
import com.casestudy.pipeline.send.DispatchOutcome;
import com.casestudy.pipeline.send.NotificationDispatcher;
import com.casestudy.time.DateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Job runner: claim due reminders, then dispatch (mock send).
 */
@Service
public class ReminderJobRunner {

    private final ReminderScheduleStore reminderScheduleStore;
    private final NotificationDispatcher notificationDispatcher;

    public ReminderJobRunner(
            ReminderScheduleStore reminderScheduleStore,
            NotificationDispatcher notificationDispatcher
    ) {
        this.reminderScheduleStore = reminderScheduleStore;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public JobRunResult runDue(int limit) {
        List<ReminderSchedule> claimed = reminderScheduleStore.claimDuePending(limit, DateTimes.now());
        List<DispatchOutcome> outcomes = new ArrayList<>();
        for (ReminderSchedule job : claimed) {
            outcomes.add(notificationDispatcher.dispatch(job));
        }
        return JobRunResult.from(claimed.size(), outcomes);
    }
}
