package com.casestudy.resources;

import com.casestudy.pipeline.dispatch.JobRunResult;
import com.casestudy.pipeline.dispatch.ReminderJobRunner;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ReminderDispatchResource {

    private final ReminderJobRunner reminderJobRunner;

    public ReminderDispatchResource(ReminderJobRunner reminderJobRunner) {
        this.reminderJobRunner = reminderJobRunner;
    }

    /**
     * Dispatch due reminders: claim → validate → mock publish → FIRED after publish succeeds.
     */
    @PostMapping("/api/reminders/dispatch")
    public JobRunResult dispatchDue(
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(100) int limit
    ) {
        return reminderJobRunner.runDue(limit);
    }
}
