package com.fitpulse.service.impl;

import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.WorkoutBooking;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingServiceImpl.class);

    private final GymClassRepository gymClassRepository;
    private final WorkoutBookingRepository bookingRepository;

    public SchedulingServiceImpl(
            GymClassRepository gymClassRepository,
            WorkoutBookingRepository bookingRepository) {

        this.gymClassRepository = gymClassRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public int removeExpiredBookings() {
        LocalDateTime cutoff = LocalDateTime.now();
        List<WorkoutBooking> expiredBookings =
                bookingRepository.findAllByGymClassStartsAtBefore(cutoff);

        bookingRepository.deleteAll(expiredBookings);

        LOGGER.info("Removed {} bookings for classes starting before {}",
                expiredBookings.size(), cutoff);
        return expiredBookings.size();
    }

    @Override
    @Transactional
    public int removePastClassesWithoutBookings() {
        LocalDateTime cutoff = LocalDateTime.now();
        List<GymClass> classesToRemove = gymClassRepository
                .findAllByStartsAtBefore(cutoff)
                .stream()
                .filter(gymClass ->
                        !bookingRepository.existsByGymClassId(gymClass.getId()))
                .toList();

        gymClassRepository.deleteAll(classesToRemove);

        LOGGER.info("Removed {} past gym classes without bookings, using cutoff {}",
                classesToRemove.size(), cutoff);
        return classesToRemove.size();
    }
}
