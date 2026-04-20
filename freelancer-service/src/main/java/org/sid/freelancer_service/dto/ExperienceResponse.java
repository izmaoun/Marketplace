package org.sid.freelancer_service.dto;

import java.time.LocalDate;

public record ExperienceResponse(
        Long id,
        String title,
        String company,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        boolean current,
        String description
) {
}
