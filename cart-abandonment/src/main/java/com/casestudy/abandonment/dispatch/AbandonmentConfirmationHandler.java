package com.casestudy.abandonment.dispatch;

import com.casestudy.abandonment.dao.CartActivityDao;
import com.casestudy.abandonment.dao.ProcessedJobDao;
import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.exception.CartNotFoundException;
import com.casestudy.abandonment.guardrail.GuardrailDecision;
import com.casestudy.abandonment.guardrail.GuardrailEngine;
import com.casestudy.abandonment.model.CartActivity;
import com.casestudy.abandonment.model.JobStatus;
import com.casestudy.abandonment.model.ScheduleJob;
import com.casestudy.abandonment.reminder.NotificationScheduler;
import com.casestudy.abandonment.time.Clock;

public final class AbandonmentConfirmationHandler implements JobHandler {

    private final GuardrailEngine guardrailEngine;
    private final NotificationScheduler notificationScheduler;
    private final CartActivityDao cartActivityDao;
    private final ScheduleDao scheduleDao;
    private final ProcessedJobDao processedJobDao;
    private final Clock clock;

    public AbandonmentConfirmationHandler(
            GuardrailEngine guardrailEngine,
            NotificationScheduler notificationScheduler,
            CartActivityDao cartActivityDao,
            ScheduleDao scheduleDao,
            ProcessedJobDao processedJobDao,
            Clock clock
    ) {
        this.guardrailEngine = guardrailEngine;
        this.notificationScheduler = notificationScheduler;
        this.cartActivityDao = cartActivityDao;
        this.scheduleDao = scheduleDao;
        this.processedJobDao = processedJobDao;
        this.clock = clock;
    }

    @Override
    public void handle(ScheduleJob job) {
        String firedKey = job.getIdempotencyKey() + ":fired";
        if (processedJobDao.alreadyProcessed(firedKey)) {
            scheduleDao.updateStatus(job.getScheduleId(), JobStatus.PROCESSED, null, clock.now());
            return;
        }
        GuardrailDecision decision = guardrailEngine.evaluate(job);
        if (!decision.accepted()) {
            scheduleDao.updateStatus(
                    job.getScheduleId(),
                    JobStatus.CANCELLED,
                    decision.suppressionReason(),
                    clock.now()
            );
            return;
        }
        CartActivity cart = cartActivityDao.findByCartId(job.getMetadata().cartId())
                .orElseThrow(() -> new CartNotFoundException(job.getMetadata().cartId()));
        notificationScheduler.scheduleReminders(cart);
        processedJobDao.markProcessed(firedKey);
        scheduleDao.updateStatus(job.getScheduleId(), JobStatus.PROCESSED, null, clock.now());
    }
}
