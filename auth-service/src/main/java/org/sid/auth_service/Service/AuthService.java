package org.sid.auth_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.sid.auth_service.Client.CompanyServiceClient;
import org.sid.auth_service.Client.FreelancerServiceClient;
import org.sid.auth_service.DTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final KeycloakService keycloakService;
    private final FreelancerServiceClient freelancerServiceClient;
    private final CompanyServiceClient companyServiceClient;
    private final CompensationService compensationService;

    public AuthService(KeycloakService keycloakService,
                       FreelancerServiceClient freelancerServiceClient,
                       CompanyServiceClient companyServiceClient,
                       CompensationService compensationService) {
        this.keycloakService = keycloakService;
        this.freelancerServiceClient = freelancerServiceClient;
        this.companyServiceClient = companyServiceClient;
        this.compensationService = compensationService;
    }

    public ResponseEntity<String> registerFreelancer(FreelancerRegisterRequest request) {
        String userId = keycloakService.createUser(
                request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName(),
                "FREELANCER"
        );

        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body("Email déjà utilisé ou erreur lors de la création du compte.");
        }

        try {
            FreelancerRequest profileData = new FreelancerRequest(
                    userId, request.getEmail(),
                    request.getFirstName(), request.getLastName(),
                    request.getPhone(), request.getSummary(),
                    request.getCvUrl(), request.getPfpUrl()
            );

            ResponseEntity<FreelancerResponse> response =
                    freelancerServiceClient.createFreelancer(profileData);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Freelancer {} enregistré avec succès (Keycloak ID: {})",
                        request.getEmail(), userId);
                return ResponseEntity.ok("Compte freelancer créé avec succès.");
            }

            log.error("Freelancer-service a retourné {} pour {} — compensation Keycloak",
                    response.getStatusCode(), request.getEmail());
            compensationService.compensate(userId, request.getEmail());
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création du profil. Veuillez réessayer.");

        } catch (Exception e) {
            log.error("Erreur appel freelancer-service pour {} : {}", request.getEmail(), e.getMessage());
            compensationService.compensate(userId, request.getEmail());
            return ResponseEntity.internalServerError()
                    .body("Erreur technique. Veuillez réessayer.");
        }
    }

    public ResponseEntity<String> registerCompany(CompanyRegisterRequest request) {
        String userId = keycloakService.createUser(
                request.getEmail(), request.getPassword(),
                request.getContactFirstName(), request.getContactLastName(),
                "COMPANY"
        );

        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body("Email déjà utilisé ou erreur lors de la création du compte.");
        }

        try {
            CompanyRequest companyData = new CompanyRequest(
                    userId, request.getEmail(),
                    request.getCompanyName(), request.getSiret(),
                    request.getContactFirstName(),
                    request.getContactLastName(),
                    "PENDING"
            );

            ResponseEntity<CompanyResponse> response =
                    companyServiceClient.createCompany(companyData);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Entreprise {} enregistrée (Keycloak ID: {}), en attente de validation.",
                        request.getCompanyName(), userId);
                return ResponseEntity.ok("Entreprise créée. En attente de validation par l'admin.");
            }

            log.error("Company-service a retourné {} pour {} — compensation Keycloak",
                    response.getStatusCode(), request.getEmail());
            compensationService.compensate(userId, request.getEmail());
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création du profil entreprise. Veuillez réessayer.");

        } catch (Exception e) {
            log.error("Erreur appel company-service pour {} : {}", request.getEmail(), e.getMessage());
            compensationService.compensate(userId, request.getEmail());
            return ResponseEntity.internalServerError()
                    .body("Erreur technique. Veuillez réessayer.");
        }
    }

    public ResponseEntity<?> login(AuthRequest request) {
        return keycloakService.login(request);
    }
}