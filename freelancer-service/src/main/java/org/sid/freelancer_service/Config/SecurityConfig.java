package org.sid.freelancer_service.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final InternalServiceTokenFilter internalServiceTokenFilter;

    public SecurityConfig(InternalServiceTokenFilter internalServiceTokenFilter) {
        this.internalServiceTokenFilter = internalServiceTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Appel interne depuis auth-service (réseau Docker backend)
                        .requestMatchers(HttpMethod.POST, "/api/freelances").hasRole("INTERNAL")
                        // Lecture publique
                        .requestMatchers(HttpMethod.GET, "/api/freelances").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/freelances/{id:\\d+}").permitAll()
                        .requestMatchers("/api/freelances/missions/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Profil personnel (authentifié)
                        .requestMatchers(HttpMethod.GET, "/api/freelances/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/freelances/me").authenticated()
                        // Admin uniquement
                        .requestMatchers(HttpMethod.GET, "/api/freelances/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/freelances/{id:\\d+}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/freelances/{id:\\d+}/suspend").hasRole("ADMIN")
                        // Tout le reste = interdit
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                // ✅ Remplace l'ancien JwtGrantedAuthoritiesConverter
                                // qui ne supportait pas realm_access.roles imbriqué
                                new KeycloakJwtAuthenticationConverter()
                        ))
                )
                .addFilterBefore(internalServiceTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
