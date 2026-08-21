package com.fitpulse.service.impl;

import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.WorkoutBooking;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.WorkoutBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceImplTest {

    @Mock
    private GymClassRepository gymClassRepository;

    @Mock
    private WorkoutBookingRepository bookingRepository;

    private SchedulingServiceImpl schedulingService;

    @BeforeEach
    void setUp() {
        schedulingService = new SchedulingServiceImpl(
                gymClassRepository, bookingRepository);
    }

    @Test
    void removeExpiredBookingsDeletesBookingsReturnedByRepository() {
        WorkoutBooking firstBooking = new WorkoutBooking();
        WorkoutBooking secondBooking = new WorkoutBooking();
        List<WorkoutBooking> expiredBookings =
                List.of(firstBooking, secondBooking);
        when(bookingRepository.findAllByGymClassStartsAtBefore(
                any(LocalDateTime.class))).thenReturn(expiredBookings);

        int removedBookings = schedulingService.removeExpiredBookings();

        assertEquals(2, removedBookings);
        verify(bookingRepository).deleteAll(expiredBookings);
    }

    @Test
    void removePastClassesDeletesOnlyClassesWithoutBookings() {
        GymClass classWithoutBookings = gymClass(UUID.randomUUID());
        GymClass classWithBookings = gymClass(UUID.randomUUID());
        when(gymClassRepository.findAllByStartsAtBefore(
                any(LocalDateTime.class))).thenReturn(
                List.of(classWithoutBookings, classWithBookings));
        when(bookingRepository.existsByGymClassId(
                classWithoutBookings.getId())).thenReturn(false);
        when(bookingRepository.existsByGymClassId(
                classWithBookings.getId())).thenReturn(true);

        int removedClasses =
                schedulingService.removePastClassesWithoutBookings();

        assertEquals(1, removedClasses);
        verify(gymClassRepository).deleteAll(List.of(classWithoutBookings));
        verify(gymClassRepository, never()).delete(classWithBookings);
    }

    private GymClass gymClass(UUID id) {
        GymClass gymClass = org.mockito.Mockito.mock(GymClass.class);
        when(gymClass.getId()).thenReturn(id);
        return gymClass;
    }
}
