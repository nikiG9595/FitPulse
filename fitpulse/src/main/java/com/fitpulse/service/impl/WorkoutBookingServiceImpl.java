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
    private final WorkoutBookingRepository bookingRepository; private final UserService userService; private final GymClassService gymClassService;
    public WorkoutBookingServiceImpl(WorkoutBookingRepository bookingRepository, UserService userService, GymClassService gymClassService) { this.bookingRepository = bookingRepository; this.userService = userService; this.gymClassService = gymClassService; }
    public void book(UUID classId) {
        User user = userService.getCurrentUser(); GymClass gymClass = gymClassService.getById(classId);
        if (user.getMembership() == null) throw new FitPulseException("Choose a membership before booking a class");
        if (bookingRepository.existsByMemberAndGymClass(user, gymClass)) throw new FitPulseException("You have already booked this class");
        if (bookingRepository.countByGymClass(gymClass) >= gymClass.getCapacity()) throw new FitPulseException("This class is fully booked");
        WorkoutBooking booking = new WorkoutBooking(); booking.setMember(user); booking.setGymClass(gymClass); booking.setBookedAt(LocalDateTime.now()); bookingRepository.save(booking);
    }
    public void cancel(UUID bookingId) {
        WorkoutBooking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new FitPulseException("Booking not found"));
        User user = userService.getCurrentUser();
        if (!booking.getMember().getId().equals(user.getId())) throw new FitPulseException("You can cancel only your own bookings");
        bookingRepository.delete(booking);
    }
    public List<WorkoutBooking> getCurrentUserBookings() { return bookingRepository.findAllByMemberOrderByBookedAtDesc(userService.getCurrentUser()); }
    public long countBookingsForClass(UUID classId) { return bookingRepository.countByGymClass(gymClassService.getById(classId)); }
}
