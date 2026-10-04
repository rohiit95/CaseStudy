package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.model.ScheduleJob;

import java.util.EnumMap;
import java.util.Map;

public final class JobHandlerRegistry {

    private final Map<JobType, JobHandler> handlers = new EnumMap<>(JobType.class);

    public JobHandlerRegistry put(JobType type, JobHandler handler) {
        handlers.put(type, handler);
        return this;
    }

    public JobHandler handler(JobType type) {
        JobHandler handler = handlers.get(type);
        if (handler == null) {
            throw new IllegalStateException("No handler for " + type);
        }
        return handler;
    }

    public void handle(ScheduleJob job) {
        handler(job.getJobType()).handle(job);
    }
}
