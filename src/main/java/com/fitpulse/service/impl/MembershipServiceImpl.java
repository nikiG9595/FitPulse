package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.dto.MembershipView;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MembershipServiceImpl implements MembershipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MembershipServiceImpl.class);

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MembershipServiceImpl(
            MembershipRepository membershipRepository,
            UserRepository userRepository,
            UserService userService) {

        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    @Cacheable("memberships")
    public List<MembershipView> getAll() {
        LOGGER.info("Loading membership list from the database");
        return membershipRepository.findAllByOrderByPriceAsc()
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Membership getById(UUID id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new FitPulseException("Membership not found"));
    }

    @Override
    @CacheEvict(value = "memberships", allEntries = true)
    public void create(MembershipRequest request) {
        if (membershipRepository.existsByType(request.getType())) {
            throw new FitPulseException("Membership type already exists");
        }

        Membership membership = new Membership();
        fill(membership, request);

        membershipRepository.save(membership);
        LOGGER.info("Membership created; membership list cache invalidated");
    }

    @Override
    @CacheEvict(value = "memberships", allEntries = true)
    public void update(UUID id, MembershipRequest request) {
        Membership membership = getById(id);

        membershipRepository.findByType(request.getType())
                .filter(existingMembership ->
                        !existingMembership.getId().equals(membership.getId()))
                .ifPresent(existingMembership -> {
                    throw new FitPulseException("Membership type already exists");
                });

        fill(membership, request);
        membershipRepository.save(membership);
        LOGGER.info("Membership {} updated; membership list cache invalidated", id);
    }

    @Override
    @CacheEvict(value = "memberships", allEntries = true)
    public void delete(UUID id) {
        Membership membership = getById(id);
        membershipRepository.delete(membership);
        LOGGER.info("Membership {} deleted; membership list cache invalidated", id);
    }

    @Override
    public MembershipRequest mapToRequest(UUID id) {
        Membership membership = getById(id);

        MembershipRequest request = new MembershipRequest();
        request.setType(membership.getType());
        request.setTitle(membership.getTitle());
        request.setPrice(membership.getPrice());
        request.setDurationDays(membership.getDurationDays());
        request.setDescription(membership.getDescription());

        return request;
    }

    @Override
    public void chooseMembership(UUID membershipId) {
        User user = userService.getCurrentUser();
        Membership membership = getById(membershipId);

        user.setMembership(membership);
        userRepository.save(user);
    }

    private void fill(Membership membership, MembershipRequest request) {
        membership.setType(request.getType());
        membership.setTitle(request.getTitle());
        membership.setPrice(request.getPrice());
        membership.setDurationDays(request.getDurationDays());
        membership.setDescription(request.getDescription());
    }

    private MembershipView toView(Membership membership) {
        return new MembershipView(
                membership.getId(),
                membership.getType(),
                membership.getTitle(),
                membership.getPrice(),
                membership.getDurationDays(),
                membership.getDescription()
        );
    }
}
