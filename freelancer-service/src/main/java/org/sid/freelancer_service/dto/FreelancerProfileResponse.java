package org.sid.freelancer_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FreelancerProfileResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String title,
        String bio,
        String location,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        boolean availableForWork,
        BigDecimal hourlyRate,
        List<SkillResponse> skills,
        List<ExperienceResponse> experiences,
        List<ProjectResponse> projects,
        boolean hasCv,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
