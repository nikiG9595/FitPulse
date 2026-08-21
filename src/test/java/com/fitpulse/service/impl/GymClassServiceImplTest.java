package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymClassServiceImplTest {
    @Mock GymClassRepository gymClassRepository;
    @Mock MembershipService membershipService;
    @Mock WorkoutBookingRepository bookingRepository;
    private GymClassServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GymClassServiceImpl(gymClassRepository, membershipService, bookingRepository);
    }

    @Test
    void getAllDelegatesToRepository() {
        List<GymClass> classes = List.of(new GymClass());
        when(gymClassRepository.findAll()).thenReturn(classes);
        assertSame(classes, service.getAll());
    }

    @Test
    void missingClassProducesBusinessException() {
        UUID id = UUID.randomUUID();
        when(gymClassRepository.findById(id)).thenReturn(Optional.empty());
        assertEquals("Class not found", assertThrows(FitPulseException.class, () -> service.getById(id)).getMessage());
    }

    @Test
    void createMapsRequestAndRequiredMembership() {
        Membership membership = new Membership();
        GymClassRequest request = request();
        when(membershipService.getById(request.getRequiredMembershipId())).thenReturn(membership);

        service.create(request);

        ArgumentCaptor<GymClass> captor = ArgumentCaptor.forClass(GymClass.class);
        verify(gymClassRepository).save(captor.capture());
        assertMapped(captor.getValue(), request, membership);
    }

    @Test
    void updateMutatesExistingClass() {
        UUID id = UUID.randomUUID();
        GymClass existing = new GymClass();
        Membership membership = new Membership();
        GymClassRequest request = request();
        request.setTitle("Updated yoga");
        when(gymClassRepository.findById(id)).thenReturn(Optional.of(existing));
        when(membershipService.getById(request.getRequiredMembershipId())).thenReturn(membership);

        service.update(id, request);

        assertMapped(existing, request, membership);
        verify(gymClassRepository).save(existing);
    }

    @Test
    void deleteRejectsClassWithBookings() {
        UUID id = UUID.randomUUID();
        GymClass gymClass = new GymClass();
        when(gymClassRepository.findById(id)).thenReturn(Optional.of(gymClass));
        when(bookingRepository.existsByGymClassId(id)).thenReturn(true);

        assertEquals("This class cannot be deleted because it has active bookings",
                assertThrows(FitPulseException.class, () -> service.delete(id)).getMessage());
        verify(gymClassRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesClassWithoutBookings() {
        UUID id = UUID.randomUUID();
        GymClass gymClass = new GymClass();
        when(gymClassRepository.findById(id)).thenReturn(Optional.of(gymClass));
        service.delete(id);
        verify(gymClassRepository).delete(gymClass);
    }

    @Test
    void mapToRequestCopiesEditableValues() {
        UUID id = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        Membership membership = mock(Membership.class);
        when(membership.getId()).thenReturn(membershipId);
        GymClass gymClass = new GymClass();
        GymClassRequest source = request();
        gymClass.setTitle(source.getTitle());
        gymClass.setTrainerName(source.getTrainerName());
        gymClass.setStartsAt(source.getStartsAt());
        gymClass.setCapacity(source.getCapacity());
        gymClass.setIntensity(source.getIntensity());
        gymClass.setDescription(source.getDescription());
        gymClass.setRequiredMembership(membership);
        when(gymClassRepository.findById(id)).thenReturn(Optional.of(gymClass));

        GymClassRequest mapped = service.mapToRequest(id);

        assertEquals(source.getTitle(), mapped.getTitle());
        assertEquals(source.getTrainerName(), mapped.getTrainerName());
        assertEquals(source.getStartsAt(), mapped.getStartsAt());
        assertEquals(source.getCapacity(), mapped.getCapacity());
        assertEquals(source.getIntensity(), mapped.getIntensity());
        assertEquals(source.getDescription(), mapped.getDescription());
        assertEquals(membershipId, mapped.getRequiredMembershipId());
    }

    private static GymClassRequest request() {
        GymClassRequest request = new GymClassRequest();
        request.setTitle("Morning yoga");
        request.setTrainerName("Alex Smith");
        request.setStartsAt(LocalDateTime.of(2030, 1, 2, 10, 0));
        request.setCapacity(12);
        request.setIntensity(ClassIntensity.LOW);
        request.setDescription("A gentle morning class");
        request.setRequiredMembershipId(UUID.randomUUID());
        return request;
    }

    private static void assertMapped(GymClass actual, GymClassRequest request, Membership membership) {
        assertAll(
                () -> assertEquals(request.getTitle(), actual.getTitle()),
                () -> assertEquals(request.getTrainerName(), actual.getTrainerName()),
                () -> assertEquals(request.getStartsAt(), actual.getStartsAt()),
                () -> assertEquals(request.getCapacity(), actual.getCapacity()),
                () -> assertEquals(request.getIntensity(), actual.getIntensity()),
                () -> assertEquals(request.getDescription(), actual.getDescription()),
                () -> assertSame(membership, actual.getRequiredMembership()));
    }
}
