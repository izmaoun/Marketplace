package org.sid.freelancer_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor

public class FreelancerProfileDTO {
        private Long id;
        private String fullname;
        private String summary;
        private List<String> skills;
        private List<String> experiences;
        private List<String> projects;
        // pas de keycloakUserId, pas de isSuspended, pas d'email
}
