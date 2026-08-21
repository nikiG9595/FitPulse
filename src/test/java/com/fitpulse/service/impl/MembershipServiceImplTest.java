package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceImplTest {
    @Mock MembershipRepository membershipRepository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    private MembershipServiceImpl service;

    @BeforeEach void setUp() { service = new MembershipServiceImpl(membershipRepository, userRepository, userService); }

    @Test
    void getByIdRejectsMissingMembership() {
        UUID id = UUID.randomUUID();
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());
        assertEquals("Membership not found", assertThrows(FitPulseException.class, () -> service.getById(id)).getMessage());
    }

    @Test
    void createRejectsDuplicateTypeAndOtherwisePersistsMappedMembership() {
        MembershipRequest request = request(MembershipType.BASIC);
        when(membershipRepository.existsByType(MembershipType.BASIC)).thenReturn(true);
        assertEquals("Membership type already exists", assertThrows(FitPulseException.class, () -> service.create(request)).getMessage());

        when(membershipRepository.existsByType(MembershipType.BASIC)).thenReturn(false);
        service.create(request);
        verify(membershipRepository).save(argThat(m -> m.getType() == MembershipType.BASIC
                && m.getTitle().equals("Basic plan") && m.getPrice().equals(new BigDecimal("25.00"))
                && m.getDurationDays() == 30 && m.getDescription().equals("Basic membership plan")));
    }

    @Test
    void updateRejectsTypeOwnedByAnotherMembership() {
        UUID id = UUID.randomUUID();
        Membership target = membership(id, MembershipType.BASIC);
        Membership other = membership(UUID.randomUUID(), MembershipType.PREMIUM);
        when(membershipRepository.findById(id)).thenReturn(Optional.of(target));
        when(membershipRepository.findByType(MembershipType.PREMIUM)).thenReturn(Optional.of(other));
        assertEquals("Membership type already exists",
                assertThrows(FitPulseException.class, () -> service.update(id, request(MembershipType.PREMIUM))).getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void updateAllowsSameMembershipAndMapToRequestCopiesValues() {
        UUID id = UUID.randomUUID();
        Membership target = membership(id, MembershipType.BASIC);
        MembershipRequest update = request(MembershipType.PREMIUM);
        when(membershipRepository.findById(id)).thenReturn(Optional.of(target));
        when(membershipRepository.findByType(MembershipType.PREMIUM)).thenReturn(Optional.of(target));
        service.update(id, update);
        verify(membershipRepository).save(target);
        assertEquals(MembershipType.PREMIUM, service.mapToRequest(id).getType());
        assertEquals("Basic plan", service.mapToRequest(id).getTitle());
    }

    @Test
    void chooseMembershipAssignsItToCurrentUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        Membership membership = membership(id, MembershipType.VIP);
        when(userService.getCurrentUser()).thenReturn(user);
        when(membershipRepository.findById(id)).thenReturn(Optional.of(membership));
        service.chooseMembership(id);
        assertSame(membership, user.getMembership());
        verify(userRepository).save(user);
    }

    private static MembershipRequest request(MembershipType type) {
        MembershipRequest r = new MembershipRequest();
        r.setType(type); r.setTitle("Basic plan"); r.setPrice(new BigDecimal("25.00"));
        r.setDurationDays(30); r.setDescription("Basic membership plan"); return r;
    }

    private static Membership membership(UUID id, MembershipType type) {
        Membership m = new Membership(); ReflectionTestUtils.setField(m, "id", id); m.setType(type);
        m.setTitle("Basic plan"); m.setPrice(new BigDecimal("25.00")); m.setDurationDays(30);
        m.setDescription("Basic membership plan"); return m;
    }
}
