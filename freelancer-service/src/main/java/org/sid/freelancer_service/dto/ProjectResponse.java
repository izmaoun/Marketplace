package org.sid.freelancer_service.dto;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        String technologies,
        String projectUrl,
        LocalDate startDate,
        LocalDate endDate
) {
}
