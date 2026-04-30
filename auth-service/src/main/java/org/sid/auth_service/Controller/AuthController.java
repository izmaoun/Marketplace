package org.sid.auth_service.Controller;

import org.sid.auth_service.DTO.AuthRequest;
import org.sid.auth_service.DTO.FreelancerRegisterRequest;
import org.sid.auth_service.DTO.CompanyRegisterRequest;
import org.sid.auth_service.Service.AuthService;
import org.sid.auth_service.Service.KeycloakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KeycloakService keycloakService;

    public AuthController(AuthService authService, KeycloakService keycloakService) {
        this.authService = authService;
        this.keycloakService = keycloakService;
    }

    @PostMapping("/freelancer/register")
    public ResponseEntity<String> registerFreelancer(@Valid @RequestBody FreelancerRegisterRequest request) {
        return authService.registerFreelancer(request);
    }

    @PostMapping("/company/register")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        return authService.registerCompany(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        return keycloakService.refreshToken(refreshToken);
    }
}