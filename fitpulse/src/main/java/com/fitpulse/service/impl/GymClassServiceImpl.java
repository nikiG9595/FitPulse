package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.WorkoutBookingRepository;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.MembershipService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GymClassServiceImpl implements GymClassService {

    private final GymClassRepository gymClassRepository;
    private final MembershipService membershipService;
    private final WorkoutBookingRepository workoutBookingRepository;

    public GymClassServiceImpl(
            GymClassRepository gymClassRepository,
            MembershipService membershipService,
            WorkoutBookingRepository workoutBookingRepository) {

        this.gymClassRepository = gymClassRepository;
        this.membershipService = membershipService;
        this.workoutBookingRepository = workoutBookingRepository;
    }

    @Override
    public List<GymClass> getAll() {
        return gymClassRepository.findAll();
    }

    @Override
    public GymClass getById(UUID id) {
        return gymClassRepository.findById(id)
                .orElseThrow(() -> new FitPulseException("Class not found"));
    }

    @Override
    public void create(GymClassRequest request) {
        GymClass gymClass = new GymClass();

        fill(gymClass, request);
        gymClassRepository.save(gymClass);
    }

    @Override
    public void update(UUID id, GymClassRequest request) {
        GymClass gymClass = getById(id);

        fill(gymClass, request);
        gymClassRepository.save(gymClass);
    }

    @Override
    public void delete(UUID id) {
        GymClass gymClass = getById(id);

        if (workoutBookingRepository.existsByGymClassId(id)) {
            throw new FitPulseException(
                    "This class cannot be deleted because it has active bookings"
            );
        }

        gymClassRepository.delete(gymClass);
    }

    @Override
    public GymClassRequest mapToRequest(UUID id) {
        GymClass gymClass = getById(id);

        GymClassRequest request = new GymClassRequest();
        request.setTitle(gymClass.getTitle());
        request.setTrainerName(gymClass.getTrainerName());
        request.setStartsAt(gymClass.getStartsAt());
        request.setCapacity(gymClass.getCapacity());
        request.setIntensity(gymClass.getIntensity());
        request.setDescription(gymClass.getDescription());
        request.setRequiredMembershipId(
                gymClass.getRequiredMembership().getId()
        );

        return request;
    }

    private void fill(GymClass gymClass, GymClassRequest request) {
        gymClass.setTitle(request.getTitle());
        gymClass.setTrainerName(request.getTrainerName());
        gymClass.setStartsAt(request.getStartsAt());
        gymClass.setCapacity(request.getCapacity());
        gymClass.setIntensity(request.getIntensity());
        gymClass.setDescription(request.getDescription());
        gymClass.setRequiredMembership(
                membershipService.getById(request.getRequiredMembershipId())
        );
    }
}