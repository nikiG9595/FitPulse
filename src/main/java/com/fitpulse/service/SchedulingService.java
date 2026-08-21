package com.fitpulse.service;

public interface SchedulingService {
    int removeExpiredBookings();

    int removePastClassesWithoutBookings();
}
