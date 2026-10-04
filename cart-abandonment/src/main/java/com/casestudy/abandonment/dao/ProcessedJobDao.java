package com.casestudy.abandonment.dao;

public interface ProcessedJobDao {

    boolean markProcessed(String idempotencyKey);

    boolean alreadyProcessed(String idempotencyKey);
}
