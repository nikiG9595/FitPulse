package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.entity.WorkoutBooking;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutBookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkoutBookingServiceImpl implements WorkoutBookingService {

    private final WorkoutBookingRepository bookingRepository;
    private final UserService userService;
    private final GymClassService gymClassService;

    public WorkoutBookingServiceImpl(
            WorkoutBookingRepository bookingRepository,
            UserService userService,
            GymClassService gymClassService) {

        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.gymClassService = gymClassService;
    }

    @Override
    public void book(UUID classId) {
        User user = userService.getCurrentUser();
        GymClass gymClass = gymClassService.getById(classId);

        if (user.getMembership() == null) {
            throw new FitPulseException(
                    "Choose a membership before booking a class"
            );
        }

        if (user.getMembership().getType().ordinal()
                < gymClass.getRequiredMembership().getType().ordinal()) {

            throw new FitPulseException(
                    "Your membership plan is not high enough for this class"
            );
        }

        if (bookingRepository.existsByMemberAndGymClass(user, gymClass)) {
            throw new FitPulseException(
                    "You have already booked this class"
            );
        }

        long currentBookings = bookingRepository.countByGymClass(gymClass);

        if (currentBookings >= gymClass.getCapacity()) {
            throw new FitPulseException(
                    "This class is fully booked"
            );
        }

        WorkoutBooking booking = new WorkoutBooking();
        booking.setMember(user);
        booking.setGymClass(gymClass);
        booking.setBookedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Override
    public void cancel(UUID bookingId) {
        WorkoutBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new FitPulseException("Booking not found")
                );

        User user = userService.getCurrentUser();

        if (!booking.getMember().getId().equals(user.getId())) {
            throw new FitPulseException(
                    "You can cancel only your own bookings"
            );
        }

        bookingRepository.delete(booking);
    }

    @Override
    public List<WorkoutBooking> getCurrentUserBookings() {
        User user = userService.getCurrentUser();

        return bookingRepository
                .findAllByMemberOrderByBookedAtDesc(user);
    }

    @Override
    public long countBookingsForClass(UUID classId) {
        GymClass gymClass = gymClassService.getById(classId);

        return bookingRepository.countByGymClass(gymClass);
    }
}