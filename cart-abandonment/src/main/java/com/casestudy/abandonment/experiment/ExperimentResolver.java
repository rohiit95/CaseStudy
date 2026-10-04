package com.casestudy.abandonment.experiment;

import com.casestudy.ab.CartReminderVariant;

public interface ExperimentResolver {

    CartReminderVariant resolve(String subjectId);
}
