package com.fitpulse.service;

import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.entity.GymClass;
import java.util.List;
import java.util.UUID;

public interface GymClassService {
    List<GymClass> getAll();
    GymClass getById(UUID id);
    void create(GymClassRequest request);
    void update(UUID id, GymClassRequest request);
    void delete(UUID id);
    GymClassRequest mapToRequest(UUID id);
}
