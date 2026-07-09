package com.fitpulse.model.dto;

import com.fitpulse.model.enums.ClassIntensity;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public class GymClassRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 80, message = "Title must be between 3 and 80 characters")
    private String title;
    @NotBlank(message = "Trainer name is required")
    @Size(min = 3, max = 60, message = "Trainer name must be between 3 and 60 characters")
    private String trainerName;
    @Future(message = "Start date must be in the future")
    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100, message = "Capacity cannot exceed 100")
    private Integer capacity;
    @NotNull(message = "Intensity is required")
    private ClassIntensity intensity;
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 900, message = "Description must be between 10 and 900 characters")
    private String description;
    @NotNull(message = "Required membership is required")
    private UUID requiredMembershipId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public ClassIntensity getIntensity() {
        return intensity;
    }

    public void setIntensity(ClassIntensity intensity) {
        this.intensity = intensity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getRequiredMembershipId() {
        return requiredMembershipId;
    }

    public void setRequiredMembershipId(UUID requiredMembershipId) {
        this.requiredMembershipId = requiredMembershipId;
    }
}
