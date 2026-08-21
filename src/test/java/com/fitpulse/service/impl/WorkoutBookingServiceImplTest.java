package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.entity.WorkoutBooking;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutBookingServiceImplTest {
    @Mock WorkoutBookingRepository bookingRepository;
    @Mock UserService userService;
    @Mock GymClassService gymClassService;
    private WorkoutBookingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkoutBookingServiceImpl(bookingRepository, userService, gymClassService);
    }

    @Test
    void bookingRequiresMembership() {
        User user = mock(User.class);
        GymClass gymClass = mock(GymClass.class);
        stubContext(user, gymClass);
        assertBusinessError("Choose a membership before booking a class", () -> service.book(UUID.randomUUID()));
    }

    @Test
    void bookingRequiresSufficientPlan() {
        User user = userWithMembership(MembershipType.BASIC);
        GymClass gymClass = classRequiring(MembershipType.PREMIUM);
        UUID classId = UUID.randomUUID();
        when(userService.getCurrentUser()).thenReturn(user);
        when(gymClassService.getById(classId)).thenReturn(gymClass);
        assertBusinessError("Your membership plan is not high enough for this class", () -> service.book(classId));
    }

    @Test
    void duplicateBookingIsRejected() {
        User user = userWithMembership(MembershipType.PREMIUM);
        GymClass gymClass = classRequiring(MembershipType.BASIC);
        UUID classId = UUID.randomUUID();
        when(userService.getCurrentUser()).thenReturn(user);
        when(gymClassService.getById(classId)).thenReturn(gymClass);
        when(bookingRepository.existsByMemberAndGymClass(user, gymClass)).thenReturn(true);
        assertBusinessError("You have already booked this class", () -> service.book(classId));
    }

    @Test
    void fullClassIsRejected() {
        User user = userWithMembership(MembershipType.PREMIUM);
        GymClass gymClass = classRequiring(MembershipType.BASIC);
        when(gymClass.getCapacity()).thenReturn(2);
        UUID classId = UUID.randomUUID();
        when(userService.getCurrentUser()).thenReturn(user);
        when(gymClassService.getById(classId)).thenReturn(gymClass);
        when(bookingRepository.countByGymClass(gymClass)).thenReturn(2L);
        assertBusinessError("This class is fully booked", () -> service.book(classId));
    }

    @Test
    void eligibleUserCanBookAvailableClass() {
        User user = userWithMembership(MembershipType.PREMIUM);
        GymClass gymClass = classRequiring(MembershipType.BASIC);
        when(gymClass.getCapacity()).thenReturn(10);
        UUID classId = UUID.randomUUID();
        when(userService.getCurrentUser()).thenReturn(user);
        when(gymClassService.getById(classId)).thenReturn(gymClass);

        service.book(classId);

        ArgumentCaptor<WorkoutBooking> captor = ArgumentCaptor.forClass(WorkoutBooking.class);
        verify(bookingRepository).save(captor.capture());
        assertSame(user, captor.getValue().getMember());
        assertSame(gymClass, captor.getValue().getGymClass());
        assertNotNull(captor.getValue().getBookedAt());
    }

    @Test
    void cancelRejectsMissingAndOtherUsersBooking() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());
        assertBusinessError("Booking not found", () -> service.cancel(id));

        WorkoutBooking booking = new WorkoutBooking();
        User owner = mock(User.class);
        User current = mock(User.class);
        booking.setMember(owner);
        when(owner.getId()).thenReturn(UUID.randomUUID());
        when(current.getId()).thenReturn(UUID.randomUUID());
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(current);
        assertBusinessError("You can cancel only your own bookings", () -> service.cancel(id));
    }

    @Test
    void ownerCanCancelBooking() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkoutBooking booking = new WorkoutBooking();
        User owner = mock(User.class);
        booking.setMember(owner);
        when(owner.getId()).thenReturn(userId);
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(owner);
        service.cancel(id);
        verify(bookingRepository).delete(booking);
    }

    @Test
    void queriesDelegateWithResolvedDomainObjects() {
        User user = mock(User.class);
        List<WorkoutBooking> bookings = List.of(new WorkoutBooking());
        when(userService.getCurrentUser()).thenReturn(user);
        when(bookingRepository.findAllByMemberOrderByBookedAtDesc(user)).thenReturn(bookings);
        assertSame(bookings, service.getCurrentUserBookings());

        UUID classId = UUID.randomUUID();
        GymClass gymClass = mock(GymClass.class);
        when(gymClassService.getById(classId)).thenReturn(gymClass);
        when(bookingRepository.countByGymClass(gymClass)).thenReturn(4L);
        assertEquals(4L, service.countBookingsForClass(classId));
    }

    private void stubContext(User user, GymClass gymClass) {
        when(userService.getCurrentUser()).thenReturn(user);
        when(gymClassService.getById(any())).thenReturn(gymClass);
    }

    private static User userWithMembership(MembershipType type) {
        User user = mock(User.class);
        Membership membership = mock(Membership.class);
        when(membership.getType()).thenReturn(type);
        when(user.getMembership()).thenReturn(membership);
        return user;
    }

    private static GymClass classRequiring(MembershipType type) {
        GymClass gymClass = mock(GymClass.class);
        Membership membership = mock(Membership.class);
        when(membership.getType()).thenReturn(type);
        when(gymClass.getRequiredMembership()).thenReturn(membership);
        return gymClass;
    }

    private static void assertBusinessError(String message, Runnable action) {
        assertEquals(message, assertThrows(FitPulseException.class, action::run).getMessage());
    }
}
