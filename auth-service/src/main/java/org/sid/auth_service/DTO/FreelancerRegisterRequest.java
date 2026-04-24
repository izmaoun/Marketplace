package org.sid.auth_service.DTO;

import lombok.Data;

@Data
public class FreelancerRegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String jobTitle; // Ex: Développeur Java, Designer UX
}