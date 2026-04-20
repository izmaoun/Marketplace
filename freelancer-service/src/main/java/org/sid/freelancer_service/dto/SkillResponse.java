package org.sid.freelancer_service.dto;

import org.sid.freelancer_service.entity.SkillLevel;

public record SkillResponse(
        Long id,
        String name,
        SkillLevel level,
        Integer yearsOfExperience
) {
}
