package com.fitpulse.model.entity;

import com.fitpulse.model.enums.MembershipType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @NotNull
    private MembershipType type;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 3, max = 50)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer durationDays;

    @Column(nullable = false, length = 800)
    @NotBlank
    @Size(min = 10, max = 800)
    private String description;

    public UUID getId() {
        return id;
    }

    public MembershipType getType() {
        return type;
    }

    public void setType(MembershipType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
