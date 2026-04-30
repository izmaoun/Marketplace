package org.sid.auth_service.Service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.sid.auth_service.DTO.AuthRequest;
import org.sid.auth_service.DTO.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Crée un utilisateur dans Keycloak et lui assigne le rôle demandé.
     */
    public String createUser(String email, String password, String firstName, String lastName, String roleName) {
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
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = keycloak.realm(realm).users();
        try (Response response = usersResource.create(user)) {
            int status = response.getStatus();
            if (status == 201) {
                String location = response.getLocation().toString();
                String userId = location.substring(location.lastIndexOf('/') + 1);

                // Assignation du rôle
                RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
                if (role != null) {
                    keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
                }
                return userId;
            }
            return null;
        } catch (Exception e) {
            log.error("Erreur création utilisateur Keycloak : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Login utilisant le builder Keycloak pour obtenir un token.
     */
    public ResponseEntity<?> login(AuthRequest request) {
        try {
            // On crée une instance temporaire pour authentifier l'utilisateur spécifique
            Keycloak userKeycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(request.getEmail())
                    .password(request.getPassword())
                    .build();

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();

            AuthResponse authResponse = new AuthResponse(
                    tokenResponse.getToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Erreur lors du login : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    /**
     * Refresh token utilisant le même principe.
     */
    public ResponseEntity<?> refreshToken(String refreshToken) {
        try {
            Keycloak refreshKeycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.REFRESH_TOKEN)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            // Note: Le client Java Admin ne gère pas nativement le refresh pur de cette manière simple,
            // mais vous pouvez utiliser le tokenManager ou rester sur un appel HTTP léger si nécessaire.
            // Pour un PFS, la méthode login est la plus cruciale.
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Fonctionnalité gérée par la passerelle ou Keycloak JS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erreur de rafraîchissement");
        }
    }

    public boolean deleteUser(String userId) {
        try {
            keycloak.realm(realm).users().delete(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}