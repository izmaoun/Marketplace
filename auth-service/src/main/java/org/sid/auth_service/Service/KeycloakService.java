package org.sid.auth_service.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.sid.auth_service.DTO.AuthRequest; // Ajout de l'import pour le login
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    // Modification de la signature : on accepte les champs individuels
    public int createUser(String email, String password, String firstName, String lastName, String role) {
        // 1. Définition du mot de passe
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        // 2. Définition de l'utilisateur
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email); // Utilisation de l'email comme identifiant
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        // On stocke le rôle (FREELANCER ou COMPANY)
        user.singleAttribute("type", role);

        UsersResource usersResource = keycloak.realm(realm).users();

        // Utilisation de try-with-resources pour fermer la réponse proprement
        try (Response response = usersResource.create(user)) {
            return response.getStatus();
        }
    }

    public Object login(AuthRequest request) {
        // À implémenter avec votre logique de récupération de token
        return null;
    }
}