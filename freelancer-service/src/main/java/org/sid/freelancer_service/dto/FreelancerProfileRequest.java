package org.sid.freelancer_service.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record FreelancerProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String email,
        String phone,
        String title,
        String bio,
        String location,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        boolean availableForWork,
        BigDecimal hourlyRate
) {
}
