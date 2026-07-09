package com.fitpulse.repository;

import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.entity.WorkoutBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkoutBookingRepository extends JpaRepository<WorkoutBooking, UUID> {
    boolean existsByMemberAndGymClass(User member, GymClass gymClass);

    long countByGymClass(GymClass gymClass);

    List<WorkoutBooking> findAllByMemberOrderByBookedAtDesc(User member);
}
