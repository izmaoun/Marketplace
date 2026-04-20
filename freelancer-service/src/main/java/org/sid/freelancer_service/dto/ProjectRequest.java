package org.sid.freelancer_service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank String title,
        String description,
        String technologies,
        String projectUrl,
        LocalDate startDate,
        LocalDate endDate
) {
}
