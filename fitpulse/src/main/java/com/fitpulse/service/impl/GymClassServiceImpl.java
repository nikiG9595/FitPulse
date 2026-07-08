package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.MembershipService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class GymClassServiceImpl implements GymClassService {
    private final GymClassRepository gymClassRepository; private final MembershipService membershipService;
    public GymClassServiceImpl(GymClassRepository gymClassRepository, MembershipService membershipService) { this.gymClassRepository = gymClassRepository; this.membershipService = membershipService; }
    public List<GymClass> getAll() { return gymClassRepository.findAll(); }
    public GymClass getById(UUID id) { return gymClassRepository.findById(id).orElseThrow(() -> new FitPulseException("Class not found")); }
    public void create(GymClassRequest request) { GymClass c = new GymClass(); fill(c, request); gymClassRepository.save(c); }
    public void update(UUID id, GymClassRequest request) { GymClass c = getById(id); fill(c, request); gymClassRepository.save(c); }
    public void delete(UUID id) { gymClassRepository.delete(getById(id)); }
    public GymClassRequest mapToRequest(UUID id) {
        GymClass c = getById(id); GymClassRequest r = new GymClassRequest();
        r.setTitle(c.getTitle()); r.setTrainerName(c.getTrainerName()); r.setStartsAt(c.getStartsAt()); r.setCapacity(c.getCapacity()); r.setIntensity(c.getIntensity()); r.setDescription(c.getDescription()); r.setRequiredMembershipId(c.getRequiredMembership().getId()); return r;
    }
    private void fill(GymClass c, GymClassRequest r) {
        c.setTitle(r.getTitle()); c.setTrainerName(r.getTrainerName()); c.setStartsAt(r.getStartsAt()); c.setCapacity(r.getCapacity()); c.setIntensity(r.getIntensity()); c.setDescription(r.getDescription()); c.setRequiredMembership(membershipService.getById(r.getRequiredMembershipId()));
    }
}
