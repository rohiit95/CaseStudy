package com.casestudy.services.impl;

import com.casestudy.models.CartEvent;
import com.casestudy.models.CartEventResult;
import com.casestudy.models.ReminderSchedule;
import com.casestudy.pipeline.detect.AbandonmentDetector;
import com.casestudy.pipeline.detect.DetectionResult;
import com.casestudy.pipeline.schedule.NotificationScheduler;
import com.casestudy.services.CartEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartEventServiceImpl implements CartEventService {

    private final AbandonmentDetector abandonmentDetector;
    private final NotificationScheduler notificationScheduler;

    public CartEventServiceImpl(
            AbandonmentDetector abandonmentDetector,
            NotificationScheduler notificationScheduler
    ) {
        this.abandonmentDetector = abandonmentDetector;
        this.notificationScheduler = notificationScheduler;
    }

    @Override
    @Transactional
    public CartEventResult process(CartEvent event) {
        DetectionResult detection = abandonmentDetector.detect(event);
        if (detection.duplicate()) {
            List<ReminderSchedule> reminders = notificationScheduler.scheduleForDuplicate(detection.cart());
            return CartEventResult.from(detection.cart(), reminders, true);
        }
        List<ReminderSchedule> reminders = notificationScheduler.scheduleIfEligible(event, detection.cart());
        return CartEventResult.from(detection.cart(), reminders, false);
    }
}
