package com.fitpulse.model.dto;

import com.fitpulse.model.enums.MembershipType;

import java.math.BigDecimal;
import java.util.UUID;

public record MembershipView(
        UUID id,
        MembershipType type,
        String title,
        BigDecimal price,
        Integer durationDays,
        String description) {
}
