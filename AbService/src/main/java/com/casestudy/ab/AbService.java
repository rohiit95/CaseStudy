package com.casestudy.ab;

import java.util.List;

public interface AbService {

    boolean isEnabled(String experimentKey, String subjectId);

    /**
     * Returns one experiment variable for the subject's bucket, or {@code defaultValue}
     * when the subject is in the default bucket or the variable is unknown.
     */
    <T> T getValue(String experimentKey, String variable, String subjectId, T defaultValue);

    /**
     * Full cart-reminder assignment for this subject. {@code defaults} (from ConfigService)
     * are used for the default bucket.
     */
    CartReminderVariant getCartReminderVariant(String subjectId, CartReminderVariant defaults);
}
