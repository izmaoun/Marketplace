package org.sid.auth_service.Service;

import lombok.RequiredArgsConstructor;
import org.sid.auth_service.DTO.CompanyRegisterRequest;
import org.sid.auth_service.DTO.FreelancerRegisterRequest;
import org.sid.auth_service.DTO.AuthRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final RestTemplate restTemplate;

    /**
     * Inscription d'un Freelancer
     * 1. Créer l'utilisateur dans Keycloak avec le rôle FREELANCER
     * 2. Créer le profil dans user-service (freelancer-service)
     */
    public String registerFreelancer(FreelancerRegisterRequest request) {
        // ═══════════════════════════════════════════════════════════
        // ÉTAPE 1 : Créer l'utilisateur dans Keycloak
        // ═══════════════════════════════════════════════════════════
        int keycloakStatus = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                "FREELANCER"
        );

        // Vérifier si la création dans Keycloak a réussi
        if (keycloakStatus != 201) {
            return handleKeycloakResponse(keycloakStatus);
        }

        // ═══════════════════════════════════════════════════════════
        // ÉTAPE 2 : Créer le profil dans user-service (freelancer-service)
        // ═══════════════════════════════════════════════════════════
        try {
            String userServiceUrl = "http://freelancer-service:8082/api/freelances";

            // Préparer les données du profil
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("email", request.getEmail());
            profileData.put("firstName", request.getFirstName());
            profileData.put("lastName", request.getLastName());
            // Utiliser les champs actuels de FreelancerRegisterRequest
            profileData.put("cvUrl", request.getCvUrl());
            profileData.put("password", request.getPassword());
            profileData.put("phone", String.valueOf(request.getPhone()));
            profileData.put("summary", request.getSummary());
            // Les autres champs (compétences, expériences, etc.) seront ajoutés plus tard par le freelancer

            // Configurer les headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(profileData, headers);

            // Appeler user-service pour créer le profil
            ResponseEntity<Map> response = restTemplate.postForEntity(userServiceUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "✅ Freelancer créé avec succès ! Vous pouvez maintenant vous connecter.";
            } else {
                return "⚠️ Compte créé dans Keycloak mais erreur lors de la création du profil. Contactez l'administrateur.";
            }

        } catch (Exception e) {
            // Si l'appel à user-service échoue, on informe l'utilisateur
            // L'utilisateur existe dans Keycloak mais son profil n'est pas complet
            return "⚠️ Compte créé dans Keycloak mais erreur lors de la création du profil : " + e.getMessage();
        }
    }

    /**
     * Inscription d'une Entreprise
     * 1. Créer l'utilisateur dans Keycloak avec le rôle COMPANY
     * 2. Créer le profil dans company-service avec le statut PENDING_VALIDATION
     */
    public String registerCompany(CompanyRegisterRequest request) {
        // ═══════════════════════════════════════════════════════════
        // ÉTAPE 1 : Créer l'utilisateur dans Keycloak
        // ═══════════════════════════════════════════════════════════
        int keycloakStatus = keycloakService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getContactPerson(),  // Prénom du contact
                request.getCompanyName(),    // Nom de l'entreprise
                "COMPANY"
        );

        // Vérifier si la création dans Keycloak a réussi
        if (keycloakStatus != 201) {
            return handleKeycloakResponse(keycloakStatus);
        }

        // ═══════════════════════════════════════════════════════════
        // ÉTAPE 2 : Créer le profil dans company-service
        // ═══════════════════════════════════════════════════════════
        try {
            String companyServiceUrl = "http://company-service:8083/api/companies";

            // Préparer les données de l'entreprise
            Map<String, Object> companyData = new HashMap<>();
            companyData.put("email", request.getEmail());
            companyData.put("companyName", request.getCompanyName());
            companyData.put("siret", request.getSiret());
            companyData.put("contactPerson", request.getContactPerson());
            companyData.put("status", "PENDING_VALIDATION"); // En attente de validation par l'admin

            // Configurer les headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(companyData, headers);

            // Appeler company-service pour créer le profil
            ResponseEntity<Map> response = restTemplate.postForEntity(companyServiceUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "✅ Entreprise créée avec succès ! Votre compte est en attente de validation par un administrateur.";
            } else {
                return "⚠️ Compte créé dans Keycloak mais erreur lors de la création du profil entreprise.";
            }

        } catch (Exception e) {
            return "⚠️ Compte créé dans Keycloak mais erreur lors de la création du profil entreprise : " + e.getMessage();
        }
    }

    /**
     * Connexion (Login)
     * Délègue tout à KeycloakService qui gère l'authentification OAuth2
     */
    public Object login(AuthRequest request) {
        return keycloakService.login(request);
    }

    /**
     * Gestion des codes de réponse HTTP de Keycloak
     */
    private String handleKeycloakResponse(int status) {
        switch (status) {
            case 201:
                return "✅ Utilisateur créé avec succès dans Keycloak";
            case 409:
                return "❌ Erreur : Cet email est déjà utilisé";
            case 400:
                return "❌ Erreur : Données invalides (vérifiez l'email et le mot de passe)";
            case 401:
                return "❌ Erreur : Authentification Keycloak échouée";
            case 403:
                return "❌ Erreur : Accès refusé par Keycloak";
            case 500:
                return "❌ Erreur serveur Keycloak";
            default:
                return "❌ Erreur inattendue lors de la création de l'utilisateur (code " + status + ")";
        }
    }
}