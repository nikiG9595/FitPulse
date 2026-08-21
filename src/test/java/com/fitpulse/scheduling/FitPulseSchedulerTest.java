package com.fitpulse.scheduling;

import com.fitpulse.service.SchedulingService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class FitPulseSchedulerTest {
    @Test
    void scheduledJobsDelegateExactlyOnce() {
        SchedulingService service = mock(SchedulingService.class);
        when(service.removeExpiredBookings()).thenReturn(3);
        when(service.removePastClassesWithoutBookings()).thenReturn(2);
        FitPulseScheduler scheduler = new FitPulseScheduler(service);
        scheduler.removeExpiredBookings();
        scheduler.removePastClassesWithoutBookings();
        verify(service).removeExpiredBookings();
        verify(service).removePastClassesWithoutBookings();
    }
}
