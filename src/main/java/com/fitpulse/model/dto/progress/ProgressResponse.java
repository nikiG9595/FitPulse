package com.fitpulse.model.dto.progress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProgressResponse(
        UUID id,
        UUID userId,
        BigDecimal weight,
        BigDecimal bodyFatPercentage,
        LocalDate recordedAt,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
