package com.fitpulse.model.dto.progress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressCreateRequest(
        UUID userId,
        BigDecimal weight,
        BigDecimal bodyFatPercentage,
        LocalDate recordedAt,
        String note
) {
}
