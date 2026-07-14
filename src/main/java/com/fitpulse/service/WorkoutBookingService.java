package com.fitpulse.service;

import com.fitpulse.model.entity.WorkoutBooking;

import java.util.List;
import java.util.UUID;

public interface WorkoutBookingService {
    void book(UUID classId);

    void cancel(UUID bookingId);

    List<WorkoutBooking> getCurrentUserBookings();

    long countBookingsForClass(UUID classId);
}
