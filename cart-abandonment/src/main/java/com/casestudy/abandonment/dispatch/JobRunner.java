package com.casestudy.abandonment.dispatch;

public interface JobRunner {

    JobRunSummary runDue(int limit);
}
