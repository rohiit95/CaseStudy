package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.JobType;

public interface JobRunner {

    JobType jobType();

    JobRunSummary runDue(int limit);

    JobRunSummary runDue(JobType jobType, int limit);
}
