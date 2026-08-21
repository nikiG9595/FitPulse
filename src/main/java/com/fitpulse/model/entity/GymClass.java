package com.fitpulse.model.entity;

import com.fitpulse.model.enums.ClassIntensity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gym_classes")
public class GymClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    @NotBlank
    @Size(min = 3, max = 80)
    private String title;

    @Column(nullable = false, length = 60)
    @NotBlank
    @Size(min = 3, max = 60)
    private String trainerName;

    @Column(nullable = false)
    @NotNull
    @Future
    private LocalDateTime startsAt;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    @Max(100)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ClassIntensity intensity;

    @Column(nullable = false, length = 900)
    @NotBlank
    @Size(min = 10, max = 900)
    private String description;

    @ManyToOne(optional = false)
    @NotNull
    private Membership requiredMembership;

    public UUID getId() {
        return id;
    }

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

    public Membership getRequiredMembership() {
        return requiredMembership;
    }

    public void setRequiredMembership(Membership requiredMembership) {
        this.requiredMembership = requiredMembership;
    }
}
