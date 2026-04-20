package org.sid.freelancer_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sid.freelancer_service.entity.SkillLevel;

public record SkillRequest(
        @NotBlank String name,
        @NotNull SkillLevel level,
        @Min(0) Integer yearsOfExperience
) {
}
