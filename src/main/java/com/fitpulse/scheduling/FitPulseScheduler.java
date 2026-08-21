package com.fitpulse.scheduling;

import com.fitpulse.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FitPulseScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FitPulseScheduler.class);

    private final SchedulingService schedulingService;

    public FitPulseScheduler(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Scheduled(cron = "${fitpulse.scheduling.expired-bookings-cron}")
    public void removeExpiredBookings() {
        LOGGER.info("Starting scheduled expired-booking cleanup");
        int removedBookings = schedulingService.removeExpiredBookings();
        LOGGER.info("Finished expired-booking cleanup; removed {} bookings",
                removedBookings);
    }

    @Scheduled(fixedDelayString = "${fitpulse.scheduling.past-classes-delay-ms}")
    public void removePastClassesWithoutBookings() {
        LOGGER.info("Starting scheduled past-class cleanup");
        int removedClasses = schedulingService.removePastClassesWithoutBookings();
        LOGGER.info("Finished past-class cleanup; removed {} gym classes",
                removedClasses);
    }
}
