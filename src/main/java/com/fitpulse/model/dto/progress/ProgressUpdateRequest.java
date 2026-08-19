package com.fitpulse.model.dto.progress;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressUpdateRequest(
        BigDecimal weight,
        BigDecimal bodyFatPercentage,
        LocalDate recordedAt,
        String note
) {
}
