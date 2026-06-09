package org.sid.application_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class FreelancerProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String fullname;
    private String fullName;
    private String summary;
    private List<String> skills;
    private List<String> experiences;
    private List<String> projects;

    public String displayName() {
        if (isUsableName(fullname)) {
            return fullname;
        }
        if (isUsableName(fullName)) {
            return fullName;
        }
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        String full = (first + " " + last).trim();
        if (isUsableName(full)) {
            return full;
        }
        return email == null ? "" : email;
    }

    private boolean isUsableName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return !normalized.contains("indisponible") && !normalized.equals("freelancer");
    }
}
