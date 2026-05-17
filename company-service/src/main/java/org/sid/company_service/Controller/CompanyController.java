package org.sid.company_service.Controller;

import lombok.RequiredArgsConstructor;
import org.sid.company_service.DTO.*;
import org.sid.company_service.Entity.Company;
import org.sid.company_service.Service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // ── Inscription : créer une entreprise ──
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.saveCompanyFromAuth(request));
    }


    @GetMapping
    public ResponseEntity<List<CompanyPublicDTO>> getAllCompanies() {
        return ResponseEntity.ok(
                companyService.getAllCompanies().stream()
                        .map(c -> new CompanyPublicDTO(c.getId(), c.getCompanyName(), c.getDomaine()))
                        .collect(Collectors.toList())
        );
    }

    // ── Mon entreprise (jwt oho)
    @GetMapping("/me")
    public ResponseEntity<Company> getMyCompany(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        return ResponseEntity.ok(companyService.getCompanyByKeycloakId(keycloakId));
    }

    @PutMapping("/me")
    public ResponseEntity<Company> updateMyCompany(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CompanyMeDTO updated) {
        String keycloakId = jwt.getSubject();
        Company mine = companyService.getCompanyByKeycloakId(keycloakId);
        return ResponseEntity.ok(companyService.updateCompany(mine.getId(), updated));
    }

    // ── Profil public ──
    @GetMapping("/{id}")
    public ResponseEntity<CompanyPublicDTO> getCompany(@PathVariable Long id) {
        Company c = companyService.getCompanyById(id);
        return ResponseEntity.ok(new CompanyPublicDTO(c.getId(), c.getCompanyName(), c.getDomaine()));
    }

    // ── Admin ──
    @GetMapping("/admin/pending")
    public ResponseEntity<List<Company>> getPending() {
        return ResponseEntity.ok(companyService.getPendingCompanies());
    }

    @PutMapping("/admin/{id}/validate")
    public ResponseEntity<Company> validate(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.validateCompany(id));
    }

    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<Company> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(companyService.rejectCompany(id, body.get("reason")));
    }

    // ── Missions ──
    @PostMapping("/missions")
    public ResponseEntity<MissionResponse> createMission(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody MissionRequest mission) {
        return ResponseEntity.ok(companyService.createMission(jwt.getSubject(), mission));
    }

    @PutMapping("/missions/{id}")
    public ResponseEntity<MissionResponse> updateMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody MissionRequest mission) {
        return companyService.updateMission(jwt.getSubject(), id, mission);
    }

    @DeleteMapping("/missions/{id}")
    public ResponseEntity<Void> deleteMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return companyService.deleteMission(jwt.getSubject(), id);
    }

    @PostMapping("/missions/{id}/publier")
    public ResponseEntity<MissionResponse> publierMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return companyService.publierMission(jwt.getSubject(), id);
    }

    @PostMapping("/missions/{id}/demarrer")
    public ResponseEntity<MissionResponse> demarrerMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return companyService.demarrerMission(jwt.getSubject(), id);
    }

    @PostMapping("/missions/{id}/cloturer")
    public ResponseEntity<MissionResponse> cloturerMission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return companyService.cloturerMission(jwt.getSubject(), id);
    }
}
