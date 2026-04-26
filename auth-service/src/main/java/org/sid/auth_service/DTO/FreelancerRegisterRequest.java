package org.sid.auth_service.DTO;

import lombok.Data;

@Data
public class FreelancerRegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private int phone;
    private String summary;
    private String password;
    private String cvUrl; // Ex: Développeur Java, Designer UX
}