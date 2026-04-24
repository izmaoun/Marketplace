package org.sid.auth_service.Controller;

import org.sid.auth_service.DTO.AuthRequest;
import org.sid.auth_service.DTO.FreelancerRegisterRequest; // À créer
import org.sid.auth_service.DTO.CompanyRegisterRequest;    // À créer
import lombok.RequiredArgsConstructor;
import org.sid.auth_service.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. Inscription Freelancer
    @PostMapping("/freelancer/register")
    public ResponseEntity<String> registerFreelancer(@RequestBody FreelancerRegisterRequest request) {
        String result = authService.registerFreelancer(request);
        return processResult(result);
    }

    // 2. Inscription Company
    @PostMapping("/company/register")
    public ResponseEntity<String> registerCompany(@RequestBody CompanyRegisterRequest request) {
        String result = authService.registerCompany(request);
        return processResult(result);
    }

    // 3. Login Unique (Admin, Freelancer, Company)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Appelle votre service qui communique avec Keycloak pour obtenir le JWT
        return (ResponseEntity<?>) authService.login(request);
    }

    // Petite méthode utilitaire pour éviter la répétition
    private ResponseEntity<String> processResult(String result) {
        if (result.contains("succès")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}