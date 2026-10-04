package com.casestudy.abandonment.dao.memory;

import com.casestudy.abandonment.dao.ProcessedJobDao;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryProcessedJobDao implements ProcessedJobDao {

    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    @Override
    public boolean markProcessed(String idempotencyKey) {
        return processed.add(idempotencyKey);
    }

    @Override
    public boolean alreadyProcessed(String idempotencyKey) {
        return processed.contains(idempotencyKey);
    }
}
