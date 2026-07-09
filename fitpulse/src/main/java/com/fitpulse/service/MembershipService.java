package com.fitpulse.service;

import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.entity.Membership;

import java.util.List;
import java.util.UUID;

public interface MembershipService {
    List<Membership> getAll();

    Membership getById(UUID id);

    void create(MembershipRequest request);

    void update(UUID id, MembershipRequest request);

    void delete(UUID id);

    MembershipRequest mapToRequest(UUID id);

    void chooseMembership(UUID membershipId);
}
