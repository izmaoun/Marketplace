package org.sid.auth_service.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver le CSRF (Indispensable pour tester avec Postman)
                .csrf(csrf -> csrf.disable())

                // 2. Configurer les autorisations
                .authorizeHttpRequests(auth -> auth
                        // On autorise TOUT ce qui commence par /auth/ sans token
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Configurer le mode Resource Server (JWT)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}