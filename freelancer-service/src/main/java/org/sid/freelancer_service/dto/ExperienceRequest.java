package org.sid.freelancer_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ExperienceRequest(
        @NotBlank String title,
        @NotBlank String company,
        String location,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        boolean current,
        String description
) {
}
