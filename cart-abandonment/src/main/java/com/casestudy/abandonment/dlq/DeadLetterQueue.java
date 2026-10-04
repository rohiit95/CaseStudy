package com.casestudy.abandonment.dlq;

import java.util.List;

public interface DeadLetterQueue {

    void enqueue(DeadLetterRecord record);

    List<DeadLetterRecord> replayable();
}
