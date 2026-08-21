package com.fitpulse.repository;

import com.fitpulse.model.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GymClassRepository extends JpaRepository<GymClass, UUID> {
    List<GymClass> findAllByStartsAtBefore(LocalDateTime cutoff);
}
