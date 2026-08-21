package com.fitpulse.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workout_bookings", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "gym_class_id"}))
public class WorkoutBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @NotNull
    private User member;

    @ManyToOne(optional = false)
    @NotNull
    private GymClass gymClass;

    @Column(nullable = false)
    @NotNull
    @PastOrPresent
    private LocalDateTime bookedAt;

    public UUID getId() {
        return id;
    }

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public GymClass getGymClass() {
        return gymClass;
    }

    public void setGymClass(GymClass gymClass) {
        this.gymClass = gymClass;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }
}
