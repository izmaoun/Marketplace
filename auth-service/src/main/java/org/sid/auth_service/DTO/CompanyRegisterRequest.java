package org.sid.auth_service.DTO;

import lombok.Data;

@Data
public class CompanyRegisterRequest {
    private String email;
    private String password;
    private String companyName;
    private String siret; // Ou un autre identifiant fiscal selon le pays
    private String contactPerson;
}
