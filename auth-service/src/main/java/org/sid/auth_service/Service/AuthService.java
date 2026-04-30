package org.sid.auth_service.Service;

import org.sid.auth_service.Client.CompanyServiceClient;
import org.sid.auth_service.Client.FreelancerServiceClient;
import org.sid.auth_service.DTO.CompanyRegisterRequest;
import org.sid.auth_service.DTO.FreelancerRegisterRequest;
import org.sid.auth_service.DTO.AuthRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final KeycloakService keycloakService;
    private final FreelancerServiceClient freelancerServiceClient;
    private final CompanyServiceClient companyServiceClient;

    public AuthService(KeycloakService keycloakService,
                       FreelancerServiceClient freelancerServiceClient,
                       CompanyServiceClient companyServiceClient) {
        this.keycloakService = keycloakService;
        this.freelancerServiceClient = freelancerServiceClient;
        this.companyServiceClient = companyServiceClient;
    }

    public ResponseEntity<String> registerFreelancer(FreelancerRegisterRequest request) {
        // 1. Création dans Keycloak
        String userId = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                "FREELANCER"   // nom exact du rôle dans Keycloak
        );
        if (userId == null) {
            return ResponseEntity.badRequest().body("❌ Erreur lors de la création du compte (email peut-être déjà utilisé)");
        }

        // 2. Création du profil métier (sans mot de passe)
        try {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("email", request.getEmail());
            profileData.put("firstName", request.getFirstName());
            profileData.put("lastName", request.getLastName());
            profileData.put("phone", String.valueOf(request.getPhone()));
            profileData.put("summary", request.getSummary());
            profileData.put("cvUrl", request.getCvUrl());
            // ⚠️ Ne surtout PAS envoyer le mot de passe

            ResponseEntity<Map<String, Object>> response = freelancerServiceClient.createFreelancer(profileData);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("✅ Freelancer créé avec succès !");
            } else {
                // Rollback : supprimer l'utilisateur Keycloak
                keycloakService.deleteUser(userId);
                return ResponseEntity.status(500).body("❌ Création du profil freelancer échouée, compte annulé.");
            }
        } catch (Exception e) {
            log.error("Erreur appel freelancer-service : {}", e.getMessage());
            keycloakService.deleteUser(userId);
            return ResponseEntity.status(500).body("❌ Erreur technique, veuillez réessayer.");
        }
    }

    public ResponseEntity<String> registerCompany(CompanyRegisterRequest request) {
        String userId = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getContactPerson(),   // prénom du contact
                request.getCompanyName(),     // nom de famille = nom entreprise (adaptable)
                "COMPANY"
        );
        if (userId == null) {
            return ResponseEntity.badRequest().body("❌ Erreur lors de la création du compte (email déjà utilisé)");
        }

        try {
            Map<String, Object> companyData = new HashMap<>();
            companyData.put("email", request.getEmail());
            companyData.put("companyName", request.getCompanyName());
            companyData.put("siret", request.getSiret());
            companyData.put("contactPerson", request.getContactPerson());
            companyData.put("status", "PENDING_VALIDATION");

            ResponseEntity<Map<String, Object>> response = companyServiceClient.createCompany(companyData);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("✅ Entreprise créée, en attente de validation admin.");
            } else {
                keycloakService.deleteUser(userId);
                return ResponseEntity.status(500).body("❌ Échec création profil entreprise, compte annulé.");
            }
        } catch (Exception e) {
            log.error("Erreur appel company-service : {}", e.getMessage());
            keycloakService.deleteUser(userId);
            return ResponseEntity.status(500).body("❌ Erreur technique, veuillez réessayer.");
        }
    }

    public ResponseEntity<?> login(AuthRequest request) {
        return keycloakService.login(request);
    }
}