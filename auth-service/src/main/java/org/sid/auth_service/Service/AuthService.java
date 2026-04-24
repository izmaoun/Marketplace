package org.sid.auth_service.Service;

import lombok.RequiredArgsConstructor;
import org.sid.auth_service.DTO.CompanyRegisterRequest;
import org.sid.auth_service.DTO.FreelancerRegisterRequest;
import org.sid.auth_service.DTO.AuthRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;

    public String registerFreelancer(FreelancerRegisterRequest request) {
        // Extraction des données du DTO Freelancer
        int status = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                "ROLE_FREELANCER"
        );
        return handleResponse(status);
    }

    public String registerCompany(CompanyRegisterRequest request) {
        // Extraction des données du DTO Company
        // On peut mettre le nom de l'entreprise dans le champ 'lastName' par exemple
        int status = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getContactPerson(), // Prénom du contact
                request.getCompanyName(),   // Nom de l'entreprise
                "ROLE_COMPANY"
        );
        return handleResponse(status);
    }

    public Object login(AuthRequest request) {
        return keycloakService.login(request);
    }

    private String handleResponse(int status) {
        if (status == 201) return "Utilisateur créé avec succès";
        if (status == 409) return "Erreur : L'utilisateur existe déjà";
        return "Erreur lors de la création de l'utilisateur";
    }
}