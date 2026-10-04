package com.casestudy.abandonment;

import com.casestudy.ab.AbService;
import com.casestudy.abandonment.dao.CartActivityDao;
import com.casestudy.abandonment.dao.ProcessedJobDao;
import com.casestudy.abandonment.dao.ScheduleDao;
import com.casestudy.abandonment.dao.memory.InMemoryCartActivityDao;
import com.casestudy.abandonment.dao.memory.InMemoryProcessedJobDao;
import com.casestudy.abandonment.dao.memory.InMemoryScheduleDao;
import com.casestudy.abandonment.detect.CartEventProcessor;
import com.casestudy.abandonment.detect.CartEventProcessorImpl;
import com.casestudy.abandonment.dispatch.AbandonmentConfirmationHandler;
import com.casestudy.abandonment.dispatch.JobHandlerRegistry;
import com.casestudy.abandonment.dispatch.JobRunner;
import com.casestudy.abandonment.dispatch.JobRunnerImpl;
import com.casestudy.abandonment.dispatch.NotificationDispatcher;
import com.casestudy.abandonment.dispatch.NotificationDispatcherImpl;
import com.casestudy.abandonment.experiment.ExperimentResolver;
import com.casestudy.abandonment.experiment.ExperimentResolverImpl;
import com.casestudy.abandonment.guardrail.GuardrailEngine;
import com.casestudy.abandonment.guardrail.GuardrailEngineImpl;
import com.casestudy.abandonment.model.JobType;
import com.casestudy.abandonment.reminder.NotificationScheduler;
import com.casestudy.abandonment.reminder.NotificationSchedulerImpl;
import com.casestudy.abandonment.scheduler.Scheduler;
import com.casestudy.abandonment.scheduler.SchedulerImpl;
import com.casestudy.abandonment.send.ChannelBusNotificationPublisher;
import com.casestudy.abandonment.send.EmailNotificationChannel;
import com.casestudy.abandonment.send.NotificationChannelRegistry;
import com.casestudy.abandonment.send.NotificationPublisher;
import com.casestudy.abandonment.send.PushNotificationChannel;
import com.casestudy.abandonment.send.SmsNotificationChannel;
import com.casestudy.abandonment.time.Clock;
import com.casestudy.config.ConfigService;

import java.util.List;

public final class CartAbandonmentModule {

    private final Clock clock;
    private final CartActivityDao cartActivityDao;
    private final ScheduleDao scheduleDao;
    private final ProcessedJobDao processedJobDao;
    private final Scheduler scheduler;
    private final CartEventProcessor cartEventProcessor;
    private final JobRunner jobRunner;
    private final NotificationDispatcher notificationDispatcher;

    public CartAbandonmentModule(Clock clock, ConfigService configService, AbService abService) {
        this(clock, configService, abService, new ChannelBusNotificationPublisher());
    }

    public CartAbandonmentModule(
            Clock clock,
            ConfigService configService,
            AbService abService,
            NotificationPublisher publisher
    ) {
        this.clock = clock;
        this.cartActivityDao = new InMemoryCartActivityDao();
        this.scheduleDao = new InMemoryScheduleDao();
        this.processedJobDao = new InMemoryProcessedJobDao();
        this.scheduler = new SchedulerImpl(scheduleDao, clock);
        ExperimentResolver experimentResolver = new ExperimentResolverImpl(abService, configService);
        this.cartEventProcessor = new CartEventProcessorImpl(cartActivityDao, scheduler, configService, clock);
        NotificationScheduler notificationScheduler =
                new NotificationSchedulerImpl(scheduler, experimentResolver, configService);
        GuardrailEngine guardrailEngine = new GuardrailEngineImpl(cartActivityDao);
        NotificationChannelRegistry channels = new NotificationChannelRegistry(List.of(
                new EmailNotificationChannel(publisher),
                new SmsNotificationChannel(publisher),
                new PushNotificationChannel(publisher)
        ));
        NotificationDispatcherImpl dispatcher = new NotificationDispatcherImpl(
                guardrailEngine,
                experimentResolver,
                channels,
                scheduleDao,
                processedJobDao,
                clock
        );
        this.notificationDispatcher = dispatcher;
        JobHandlerRegistry handlers = new JobHandlerRegistry()
                .put(JobType.ABANDONMENT_CONFIRM, new AbandonmentConfirmationHandler(
                        guardrailEngine,
                        notificationScheduler,
                        cartActivityDao,
                        scheduleDao,
                        processedJobDao,
                        clock
                ))
                .put(JobType.REMINDER, dispatcher);
        this.jobRunner = new JobRunnerImpl(scheduler, handlers);
    }

    public Clock clock() {
        return clock;
    }

    public CartActivityDao cartActivityDao() {
        return cartActivityDao;
    }

    public ScheduleDao scheduleDao() {
        return scheduleDao;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public CartEventProcessor cartEventProcessor() {
        return cartEventProcessor;
    }

    public JobRunner jobRunner() {
        return jobRunner;
    }

    public NotificationDispatcher notificationDispatcher() {
        return notificationDispatcher;
    }
}
