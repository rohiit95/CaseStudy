package com.casestudy.abandonment.dispatch;

public record JobRunSummary(int claimed, int handled, int failed) {

    public JobRunSummary(int claimed) {
        this(claimed, claimed, 0);
    }
}
