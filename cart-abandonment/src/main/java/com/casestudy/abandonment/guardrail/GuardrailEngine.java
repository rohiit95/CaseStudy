package com.casestudy.abandonment.guardrail;

import com.casestudy.abandonment.model.ScheduleJob;

public interface GuardrailEngine {

    GuardrailDecision evaluate(ScheduleJob job);
}
