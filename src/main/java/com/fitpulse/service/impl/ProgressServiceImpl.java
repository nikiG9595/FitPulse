package com.fitpulse.service.impl;

import com.fitpulse.client.ProgressClient;
import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressCreateRequest;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.model.dto.progress.ProgressUpdateRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.service.ProgressService;
import com.fitpulse.service.UserService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProgressServiceImpl implements ProgressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressServiceImpl.class);

    private final ProgressClient progressClient;
    private final UserService userService;

    public ProgressServiceImpl(ProgressClient progressClient, UserService userService) {
        this.progressClient = progressClient;
        this.userService = userService;
    }

    @Override
    public List<ProgressResponse> getCurrentUserProgress() {
        UUID userId = currentUser().getId();
        try {
            return progressClient.getByUserId(userId);
        } catch (FeignException exception) {
            throw translate(exception);
        }
    }

    @Override
    public ProgressResponse getCurrentUserProgress(UUID id) {
        return ownedRecord(id);
    }

    @Override
    public void create(ProgressFormRequest request) {
        User user = currentUser();
        try {
            ProgressResponse created = progressClient.create(new ProgressCreateRequest(
                    user.getId(), request.getWeight(), request.getBodyFatPercentage(), request.getRecordedAt(), request.getNote()));
            LOGGER.info("User {} created progress record {}", user.getId(), created.id());
        } catch (FeignException exception) {
            throw translate(exception);
        }
    }

    @Override
    public void update(UUID id, ProgressFormRequest request) {
        User user = currentUser();
        verifyOwnership(id, user.getId());
        try {
            progressClient.update(id, new ProgressUpdateRequest(
                    request.getWeight(), request.getBodyFatPercentage(), request.getRecordedAt(), request.getNote()));
            LOGGER.info("User {} updated progress record {}", user.getId(), id);
        } catch (FeignException exception) {
            throw translate(exception);
        }
    }

    @Override
    public void delete(UUID id) {
        User user = currentUser();
        verifyOwnership(id, user.getId());
        try {
            progressClient.delete(id);
            LOGGER.info("User {} deleted progress record {}", user.getId(), id);
        } catch (FeignException exception) {
            throw translate(exception);
        }
    }

    private ProgressResponse ownedRecord(UUID id) {
        User user = currentUser();
        return fetchOwnedRecord(id, user.getId());
    }

    private void verifyOwnership(UUID id, UUID userId) {
        fetchOwnedRecord(id, userId);
    }

    private ProgressResponse fetchOwnedRecord(UUID id, UUID userId) {
        try {
            return progressClient.getByUserId(userId).stream()
                    .filter(record -> record.id().equals(id) && record.userId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new ProgressServiceException("Progress record not found."));
        } catch (FeignException exception) {
            throw translate(exception);
        }
    }

    private User currentUser() {
        return userService.getCurrentUser();
    }

    private ProgressServiceException translate(FeignException exception) {
        if (exception.status() == 400) {
            return new ProgressServiceException("The progress record was rejected. Please check the entered values.");
        }
        if (exception.status() == 404) {
            return new ProgressServiceException("Progress record not found.");
        }
        if (exception.status() < 0) {
            return new ProgressServiceException("The progress service is currently unavailable. Please try again later.");
        }
        return new ProgressServiceException("The progress service could not complete the request. Please try again later.");
    }
}
