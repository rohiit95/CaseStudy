package com.casestudy.abandonment.dlq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryDeadLetterQueue implements DeadLetterQueue {

    private final List<DeadLetterRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void enqueue(DeadLetterRecord record) {
        records.add(record);
    }

    @Override
    public List<DeadLetterRecord> replayable() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }
}
