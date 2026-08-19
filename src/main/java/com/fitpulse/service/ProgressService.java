package com.fitpulse.service;

import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;

import java.util.List;
import java.util.UUID;

public interface ProgressService {
    List<ProgressResponse> getCurrentUserProgress();

    ProgressResponse getCurrentUserProgress(UUID id);

    void create(ProgressFormRequest request);

    void update(UUID id, ProgressFormRequest request);

    void delete(UUID id);
}
