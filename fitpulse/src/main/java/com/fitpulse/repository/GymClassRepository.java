package com.fitpulse.repository;

import com.fitpulse.model.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GymClassRepository extends JpaRepository<GymClass, UUID> {
}
