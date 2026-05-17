package org.sid.auth_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.sid.auth_service.DTO.AuthRequest;
import org.sid.auth_service.DTO.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class KeycloakService {

    private final Keycloak adminKeycloakClient;
    private final RestTemplate restTemplate;
    private final Set<String> cachedRoles = ConcurrentHashMap.newKeySet();

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.user-client-id}")
    private String userClientId;

    @Value("${keycloak.user-client-secret}")
    private String userClientSecret;

    @Value("${keycloak.email-verification-required:false}")
    private boolean emailVerificationRequired;

    public KeycloakService(Keycloak keycloak, RestTemplate restTemplate) {
        this.adminKeycloakClient = keycloak;
        this.restTemplate = restTemplate;
    }

    public String createUser(String email, String password,
                             String firstName, String lastName, String roleName) {
        ensureRoleExists(roleName);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(!emailVerificationRequired);
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = adminKeycloakClient.realm(realm).users();
        try (Response response = usersResource.create(user)) {
            int status = response.getStatus();
            if (status == 201) {
                String location = response.getLocation().toString();
                String userId = location.substring(location.lastIndexOf('/') + 1);
                log.info("Utilisateur {} créé avec ID {}", email, userId);

                RoleRepresentation role = adminKeycloakClient.realm(realm)
                        .roles().get(roleName).toRepresentation();
                adminKeycloakClient.realm(realm).users()
                        .get(userId).roles().realmLevel()
                        .add(Collections.singletonList(role));
                log.info("Rôle {} assigné à {}", roleName, email);
                return userId;
            } else if (status == 409) {
                log.warn("Email {} déjà utilisé dans Keycloak (HTTP 409)", email);
                return null;
            } else {
                log.error("Erreur création utilisateur Keycloak, HTTP status={}", status);
                return null;
            }
        } catch (Exception e) {
            log.error("Exception création utilisateur Keycloak : {}", e.getMessage());
            return null;
        }
    }

    public ResponseEntity<?> login(AuthRequest request) {
        try {
            Map<String, Object> tokenMap = callTokenEndpoint(
                    "grant_type", "password",
                    "username", request.getEmail(),
                    "password", request.getPassword()
            );
            return ResponseEntity.ok(buildAuthResponse(tokenMap));
        } catch (HttpClientErrorException e) {
            log.warn("Échec login pour {} : HTTP {}", request.getEmail(), e.getStatusCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        } catch (Exception e) {
            log.error("Erreur technique login pour {} : {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur technique lors de l'authentification");
        }
    }

    public ResponseEntity<?> refreshToken(String refreshToken) {
        try {
            Map<String, Object> tokenMap = callTokenEndpoint(
                    "grant_type", "refresh_token",
                    "refresh_token", refreshToken
            );
            return ResponseEntity.ok(buildAuthResponse(tokenMap));
        } catch (HttpClientErrorException e) {
            log.warn("Refresh token invalide ou expiré : HTTP {}", e.getStatusCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalide ou expiré");
        } catch (Exception e) {
            log.error("Erreur technique refresh token : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur technique lors du rafraîchissement du token");
        }
    }

    public boolean deleteUser(String userId) {
        try (Response response = adminKeycloakClient.realm(realm).users().delete(userId)) {
            boolean ok = response.getStatus() == 204;
            if (ok) log.info("Utilisateur {} supprimé de Keycloak (compensation saga)", userId);
            else log.error("Échec suppression utilisateur {} : HTTP {}", userId, response.getStatus());
            return ok;
        } catch (Exception e) {
            log.error("Exception suppression utilisateur {} : {}", userId, e.getMessage());
            return false;
        }
    }

    private void ensureRoleExists(String roleName) {
        if (cachedRoles.contains(roleName)) return;
        try {
            adminKeycloakClient.realm(realm).roles().get(roleName).toRepresentation();
            log.debug("Rôle {} déjà présent dans Keycloak", roleName);
        } catch (Exception e) {
            log.info("Création du rôle {} dans Keycloak", roleName);
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            adminKeycloakClient.realm(realm).roles().create(role);
        }
        cachedRoles.add(roleName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTokenEndpoint(String... kvPairs) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", userClientId);
        params.add("client_secret", userClientSecret);
        for (int i = 0; i < kvPairs.length - 1; i += 2) {
            params.add(kvPairs[i], kvPairs[i + 1]);
        }

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, httpEntity, Map.class);
        return response.getBody();
    }

    private AuthResponse buildAuthResponse(Map<String, Object> tokenMap) {
        return new AuthResponse(
                (String) tokenMap.get("access_token"),
                (String) tokenMap.get("refresh_token"),
                ((Number) tokenMap.get("expires_in")).longValue()
        );
    }
}