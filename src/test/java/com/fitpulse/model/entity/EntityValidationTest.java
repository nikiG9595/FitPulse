package com.fitpulse.model.entity;

import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.model.enums.MembershipType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void userRejectsBlankOrMalformedIdentityFieldsAndMissingRole() {
        User user = new User();
        user.setUsername("  ");
        user.setEmail("not-an-email");
        user.setPassword("  ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertViolated(violations, "username");
        assertViolated(violations, "email");
        assertViolated(violations, "password");
        assertViolated(violations, "role");
    }

    @Test
    void membershipRejectsNonPositiveCommercialValuesAndShortText() {
        Membership membership = new Membership();
        membership.setType(MembershipType.BASIC);
        membership.setTitle("AB");
        membership.setPrice(BigDecimal.ZERO);
        membership.setDurationDays(0);
        membership.setDescription("Too short");

        Set<ConstraintViolation<Membership>> violations = validator.validate(membership);

        assertViolated(violations, "title");
        assertViolated(violations, "price");
        assertViolated(violations, "durationDays");
        assertViolated(violations, "description");
    }

    @Test
    void gymClassRejectsPastStartAndCapacityOutsideSupportedRange() {
        GymClass gymClass = validGymClass();
        gymClass.setStartsAt(LocalDateTime.now().minusMinutes(1));
        gymClass.setCapacity(101);

        Set<ConstraintViolation<GymClass>> violations = validator.validate(gymClass);

        assertViolated(violations, "startsAt");
        assertViolated(violations, "capacity");
    }

    @Test
    void bookingRequiresBothSidesAndRejectsFutureBookingTimestamp() {
        WorkoutBooking booking = new WorkoutBooking();
        booking.setBookedAt(LocalDateTime.now().plusMinutes(1));

        Set<ConstraintViolation<WorkoutBooking>> violations = validator.validate(booking);

        assertViolated(violations, "member");
        assertViolated(violations, "gymClass");
        assertViolated(violations, "bookedAt");
    }

    private static GymClass validGymClass() {
        Membership membership = new Membership();
        membership.setType(MembershipType.BASIC);
        membership.setTitle("Basic plan");
        membership.setPrice(new BigDecimal("25.00"));
        membership.setDurationDays(30);
        membership.setDescription("Basic membership plan");

        GymClass gymClass = new GymClass();
        gymClass.setTitle("Morning yoga");
        gymClass.setTrainerName("Alex Smith");
        gymClass.setStartsAt(LocalDateTime.now().plusDays(1));
        gymClass.setCapacity(12);
        gymClass.setIntensity(ClassIntensity.LOW);
        gymClass.setDescription("A gentle morning workout");
        gymClass.setRequiredMembership(membership);
        return gymClass;
    }

    private static void assertViolated(Set<? extends ConstraintViolation<?>> violations, String property) {
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(property)),
                () -> "Expected a constraint violation for property: " + property);
    }
}
