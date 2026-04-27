package org.sid.auth_service.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.sid.auth_service.DTO.AuthRequest;
import org.sid.auth_service.DTO.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;
    private final RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /**
     * Créer un utilisateur dans Keycloak via l'Admin API
     *
     * @param email Email de l'utilisateur (utilisé aussi comme username)
     * @param password Mot de passe en clair (Keycloak va le hasher)
     * @param firstName Prénom
     * @param lastName Nom de famille
     * @param role Rôle de l'utilisateur (FREELANCER ou COMPANY)
     * @return Code HTTP de la réponse Keycloak (201 = succès, 409 = déjà existe, etc.)
     */
    public int createUser(String email, String password, String firstName, String lastName, String role) {
        // 1. Définir le mot de passe
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        // 2. Informations utilisateur
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);  // ← correction : point-virgule, pas de "user.cvUrl" après
        user.setCredentials(Collections.singletonList(credential));

        // Attribut personnalisé pour le rôle
        user.singleAttribute("role", role);

        // 3. Appel API Keycloak
        UsersResource usersResource = keycloak.realm(realm).users();
        try (Response response = usersResource.create(user)) {
            int status = response.getStatus();
            if (status == 201) {
                System.out.println("✅ Utilisateur créé dans Keycloak : " + email + " avec le rôle " + role);
            } else if (status == 409) {
                System.out.println("⚠️ L'utilisateur existe déjà : " + email);
            } else {
                System.out.println("❌ Erreur Keycloak (code " + status + ") pour : " + email);
            }
            return status;
        }
    }

    /**
     * Authentification via Keycloak (OAuth2 Password Grant)
     *
     * @param request Objet contenant email et password
     * @return ResponseEntity contenant le JWT (access_token + refresh_token) ou une erreur
     */
    public ResponseEntity<?> login(AuthRequest request) {
        // ═══════════════════════════════════════════════════════════
        // 1. Construire l'URL du endpoint OAuth2 de Keycloak
        // ═══════════════════════════════════════════════════════════
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        // ═══════════════════════════════════════════════════════════
        // 2. Préparer les paramètres de la requête OAuth2 Password Grant
        // ═══════════════════════════════════════════════════════════
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");             // Type de grant OAuth2
        formData.add("username", request.getEmail());       // Email de l'utilisateur
        formData.add("password", request.getPassword());    // Mot de passe
        formData.add("client_id", clientId);                // Client ID configuré dans Keycloak (auth-service)
        formData.add("client_secret", clientSecret);        // Client Secret

        // ═══════════════════════════════════════════════════════════
        // 3. Configurer les headers HTTP
        // ═══════════════════════════════════════════════════════════
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        // ═══════════════════════════════════════════════════════════
        // 4. Appeler Keycloak pour obtenir le JWT
        // ═══════════════════════════════════════════════════════════
        try {
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            // Vérifier si la requête a réussi
            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("❌ Email ou mot de passe incorrect");
            }

            // ═══════════════════════════════════════════════════════════
            // 5. Extraire les tokens de la réponse
            // ═══════════════════════════════════════════════════════════
            Map<String, Object> responseBody = tokenResponse.getBody();

            String accessToken = (String) responseBody.get("access_token");
            String refreshToken = (String) responseBody.get("refresh_token");
            Number expiresIn = (Number) responseBody.get("expires_in");

            // ═══════════════════════════════════════════════════════════
            // 6. Construire la réponse à renvoyer au frontend
            // ═══════════════════════════════════════════════════════════
            AuthResponse authResponse = new AuthResponse(
                    accessToken,
                    refreshToken,
                    expiresIn != null ? expiresIn.longValue() : 3600L
            );

            System.out.println("✅ Login réussi pour : " + request.getEmail());

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            // Gestion des erreurs (mauvais credentials, Keycloak inaccessible, etc.)
            System.err.println("❌ Erreur lors du login : " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ Erreur d'authentification : " + e.getMessage());
        }
    }

    /**
     * Rafraîchir le token JWT (Refresh Token Flow)
     * À implémenter plus tard si nécessaire
     *
     * @param refreshToken Le refresh token obtenu lors du login
     * @return Un nouveau access_token
     */
    public ResponseEntity<?> refreshToken(String refreshToken) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                String newAccessToken = (String) body.get("access_token");
                String newRefreshToken = (String) body.get("refresh_token");
                Number expiresIn = (Number) body.get("expires_in");

                AuthResponse authResponse = new AuthResponse(
                        newAccessToken,
                        newRefreshToken,
                        expiresIn != null ? expiresIn.longValue() : 3600L
                );

                return ResponseEntity.ok(authResponse);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Refresh token invalide");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ Erreur lors du rafraîchissement du token : " + e.getMessage());
        }
    }
}