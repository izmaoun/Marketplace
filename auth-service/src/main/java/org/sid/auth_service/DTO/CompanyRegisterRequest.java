package org.sid.auth_service.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegisterRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String companyName;

    @NotBlank
    private String siret;

    @NotBlank
    private String contactPerson; // prénom et nom de la personne contact

    // Lombok génère getters/setters, mais si vous évitez Lombok, ajoutez-les manuellement
}