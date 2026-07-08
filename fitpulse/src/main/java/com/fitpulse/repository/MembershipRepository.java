package com.fitpulse.repository;

import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.enums.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    Optional<Membership> findByType(MembershipType type);
    boolean existsByType(MembershipType type);
}
