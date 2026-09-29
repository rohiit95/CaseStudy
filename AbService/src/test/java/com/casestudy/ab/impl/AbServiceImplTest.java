package com.casestudy.ab.impl;

import com.casestudy.ab.AbBucket;
import com.casestudy.ab.AbVariables;
import com.casestudy.ab.CartReminderVariant;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AbServiceImplTest {

    private static final CartReminderVariant DEFAULTS = new CartReminderVariant(
            List.of(30, 60, 1440),
            "cart-abandoned-default",
            true
    );

    private static final Map<AbBucket, CartReminderVariant> VARIANT_BY_BUCKET;

    static {
        Map<AbBucket, CartReminderVariant> variants = new LinkedHashMap<>();
        variants.put(AbBucket.DEFAULT, DEFAULTS);
        variants.put(AbBucket.TEST_1, AbServiceImpl.TEST_1);
        variants.put(AbBucket.TEST_2, AbServiceImpl.TEST_2);
        variants.put(AbBucket.TEST_3, AbServiceImpl.TEST_3);
        variants.put(AbBucket.TEST_4, AbServiceImpl.TEST_4);
        VARIANT_BY_BUCKET = Map.copyOf(variants);
    }

    private final AbServiceImpl abService = new AbServiceImpl();

    @Test
    void assignsSubjectsAcrossFiveBuckets() {
        EnumSet<AbBucket> seen = IntStream.range(0, 200)
                .mapToObj(i -> "user-" + i)
                .map(id -> abService.assignBucket(AbServiceImpl.CART_REMINDERS_EXPERIMENT, id))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AbBucket.class)));

        assertThat(seen).containsExactlyInAnyOrder(AbBucket.values());
    }

    @Test
    void eachBucketVariesWindowsTemplateAndEnabled() {
        for (AbBucket bucket : AbBucket.values()) {
            String subject = subjectIn(bucket);
            CartReminderVariant expected = VARIANT_BY_BUCKET.get(bucket);
            CartReminderVariant assigned = abService.getCartReminderVariant(subject, DEFAULTS);

            assertThat(assigned).isEqualTo(expected);
            assertThat(abService.getValue(
                    AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                    AbVariables.REMINDER_WINDOWS,
                    subject,
                    DEFAULTS.reminderWindows()
            )).isEqualTo(expected.reminderWindows());
            assertThat(abService.getValue(
                    AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                    AbVariables.MESSAGE_TEMPLATE,
                    subject,
                    DEFAULTS.messageTemplate()
            )).isEqualTo(expected.messageTemplate());
            assertThat(abService.getValue(
                    AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                    AbVariables.REMINDER_ENABLED,
                    subject,
                    DEFAULTS.reminderEnabled()
            )).isEqualTo(expected.reminderEnabled());
            assertThat(abService.isEnabled(AbServiceImpl.CART_REMINDERS_EXPERIMENT, subject)).isTrue();
        }
    }

    @Test
    void defaultBucketUsesCallerProvidedDefaults() {
        String controlSubject = subjectIn(AbBucket.DEFAULT);
        assertThat(abService.getCartReminderVariant(controlSubject, DEFAULTS)).isEqualTo(DEFAULTS);
        assertThat(abService.getValue(
                AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                AbVariables.MESSAGE_TEMPLATE,
                controlSubject,
                "fallback-template"
        )).isEqualTo("fallback-template");
    }

    @Test
    void returnsDefaultWhenSubjectIsBlank() {
        assertThat(abService.assignBucket(AbServiceImpl.CART_REMINDERS_EXPERIMENT, "  "))
                .isEqualTo(AbBucket.DEFAULT);
        assertThat(abService.getCartReminderVariant("  ", DEFAULTS)).isEqualTo(DEFAULTS);
        assertThat(abService.getValue(
                AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                AbVariables.REMINDER_ENABLED,
                null,
                Boolean.TRUE
        )).isTrue();
    }

    @Test
    void returnsDefaultForUnknownExperimentOrVariable() {
        assertThat(abService.assignBucket("unknown-flag", "user-1")).isEqualTo(AbBucket.DEFAULT);
        assertThat(abService.getValue(
                "unknown-flag",
                AbVariables.REMINDER_WINDOWS,
                "user-1",
                DEFAULTS.reminderWindows()
        )).isEqualTo(DEFAULTS.reminderWindows());
        assertThat(abService.getValue(
                AbServiceImpl.CART_REMINDERS_EXPERIMENT,
                "unknownVariable",
                subjectIn(AbBucket.TEST_1),
                "fallback"
        )).isEqualTo("fallback");
    }

    @Test
    void fourTestBucketsHaveDistinctVariableSets() {
        assertThat(List.of(
                AbServiceImpl.TEST_1,
                AbServiceImpl.TEST_2,
                AbServiceImpl.TEST_3,
                AbServiceImpl.TEST_4
        )).doesNotHaveDuplicates()
                .doesNotContain(DEFAULTS);
    }

    @Test
    void disablesUnknownExperiments() {
        assertThat(abService.isEnabled("unknown-flag", "user-1")).isFalse();
    }

    private String subjectIn(AbBucket bucket) {
        return IntStream.range(0, 200)
                .mapToObj(i -> "user-" + i)
                .filter(id -> abService.assignBucket(AbServiceImpl.CART_REMINDERS_EXPERIMENT, id) == bucket)
                .findFirst()
                .orElseThrow();
    }
}
