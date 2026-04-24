package org.sid.auth_service.Config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //Annotaion pour Spring de definition de Beans que Spring va gerer
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}") //L'adresse URL de Keycloak http://localhost:8080
    private String serverUrl;

    @Value("${keycloak.realm}") //Le nom du Royaume (realm) dans Keycloa c'est l'espace de travail pour le projet (b2b-platform)
    private String realm;

    @Value("${keycloak.client-id}") //L'id du client que nous avons creer pour se microservice dans Keycloak auth-service
    private String clientId;

    @Value("${keycloak.client-secret}") //La cle secrete du client auth-service a partir de Keycloak
    private String clientSecret;

    @Bean //La creation du Bean Keycloak par Spring
    public Keycloak keycloak() { //Methode retournant un objet de type Keycloak gere par Spring
        return KeycloakBuilder.builder() //Utilisation du pattern builder avec la technique du chainage pour la creation de l'objet Keycloak avec l'ensemble des parametres
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //Connexion en mode "Client Credentials" pour les interactions d'administration
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}