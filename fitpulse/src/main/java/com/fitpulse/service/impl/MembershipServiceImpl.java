package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MembershipServiceImpl implements MembershipService {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MembershipServiceImpl(MembershipRepository membershipRepository, UserRepository userRepository, UserService userService) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Membership> getAll() {
        return membershipRepository.findAllByOrderByPriceAsc();
    }

    public Membership getById(UUID id) {
        return membershipRepository.findById(id).orElseThrow(() -> new FitPulseException("Membership not found"));
    }

    public void create(MembershipRequest request) {
        if (membershipRepository.existsByType(request.getType()))
            throw new FitPulseException("Membership type already exists");
        Membership m = new Membership();
        fill(m, request);
        membershipRepository.save(m);
    }

    public void update(UUID id, MembershipRequest request) {
        Membership m = getById(id);
        fill(m, request);
        membershipRepository.save(m);
    }

    public void delete(UUID id) {
        membershipRepository.delete(getById(id));
    }

    public MembershipRequest mapToRequest(UUID id) {
        Membership m = getById(id);
        MembershipRequest r = new MembershipRequest();
        r.setType(m.getType());
        r.setTitle(m.getTitle());
        r.setPrice(m.getPrice());
        r.setDurationDays(m.getDurationDays());
        r.setDescription(m.getDescription());
        return r;
    }

    public void chooseMembership(UUID membershipId) {
        User user = userService.getCurrentUser();
        user.setMembership(getById(membershipId));
        userRepository.save(user);
    }

    private void fill(Membership m, MembershipRequest r) {
        m.setType(r.getType());
        m.setTitle(r.getTitle());
        m.setPrice(r.getPrice());
        m.setDurationDays(r.getDurationDays());
        m.setDescription(r.getDescription());
    }
}
