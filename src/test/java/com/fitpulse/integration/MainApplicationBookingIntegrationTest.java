package com.fitpulse.integration;

import com.fitpulse.client.ProgressClient;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.entity.WorkoutBooking;
import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.WorkoutBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
@Transactional
class MainApplicationBookingIntegrationTest {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymClassRepository gymClassRepository;

    @Autowired
    private WorkoutBookingRepository workoutBookingRepository;

    @Autowired
    private WorkoutBookingService workoutBookingService;

    @MockitoBean
    private ProgressClient progressClient;

    @Test
    @WithMockUser(username = "integration-member")
    void bookingEligibleClassPersistsBookingForAuthenticatedMember() {
        Membership membership = new Membership();
        membership.setType(MembershipType.PREMIUM);
        membership.setTitle("Integration Premium");
        membership.setPrice(new BigDecimal("49.99"));
        membership.setDurationDays(30);
        membership.setDescription("Membership created only for this integration test");
        membership = membershipRepository.saveAndFlush(membership);

        User member = new User();
        member.setUsername("integration-member");
        member.setEmail("integration-member@example.test");
        member.setPassword("encoded-password-not-used-by-this-test");
        member.setRole(UserRole.MEMBER);
        member.setMembership(membership);
        member = userRepository.saveAndFlush(member);

        GymClass gymClass = new GymClass();
        gymClass.setTitle("Integration Strength Class");
        gymClass.setTrainerName("Integration Trainer");
        gymClass.setStartsAt(LocalDateTime.of(2099, 1, 15, 10, 0));
        gymClass.setCapacity(5);
        gymClass.setIntensity(ClassIntensity.HIGH);
        gymClass.setDescription("Class created only for this integration test");
        gymClass.setRequiredMembership(membership);
        gymClass = gymClassRepository.saveAndFlush(gymClass);

        workoutBookingService.book(gymClass.getId());
        workoutBookingRepository.flush();

        List<WorkoutBooking> persistedBookings = workoutBookingRepository.findAll();
        assertThat(persistedBookings).hasSize(1);

        WorkoutBooking persistedBooking = persistedBookings.getFirst();
        assertThat(persistedBooking.getId()).isNotNull();
        assertThat(persistedBooking.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistedBooking.getGymClass().getId()).isEqualTo(gymClass.getId());
        assertThat(persistedBooking.getBookedAt()).isNotNull();
        assertThat(workoutBookingRepository.countByGymClass(gymClass)).isEqualTo(1);
    }
}
