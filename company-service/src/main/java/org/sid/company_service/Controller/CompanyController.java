package org.sid.company_service.Controller;

import lombok.RequiredArgsConstructor;
import org.sid.company_service.Entity.Company;
import org.sid.company_service.Service.CompanyService;
import org.sid.company_service.Service.dto.MissionRequest;
import org.sid.company_service.Service.dto.MissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // ── Inscription : créer une entreprise ──
    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return ResponseEntity.ok(companyService.saveCompany(company));
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    // ── Mon entreprise (jwt oho)
    @GetMapping("/me")
    public ResponseEntity<Company> getMyCompany(@RequestParam String keycloakId) {
        return ResponseEntity.ok(companyService.getCompanyByKeycloakId(keycloakId));
    }

    @PutMapping("/me")
    public ResponseEntity<Company> updateMyCompany(
            @RequestParam String keycloakId,
            @RequestBody Company updated) {
        Company mine = companyService.getCompanyByKeycloakId(keycloakId);
        return ResponseEntity.ok(companyService.updateCompany(mine.getId(), updated));
    }

    // ── Profil public ──
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
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
    public ResponseEntity<MissionResponse> createMission(@RequestBody MissionRequest mission) {
        return ResponseEntity.ok(companyService.createMission(mission));
    }

    @PutMapping("/missions/{id}")
    public ResponseEntity<MissionResponse> updateMission(
            @PathVariable Long id,
            @RequestBody MissionRequest mission) {
        return companyService.updateMission(id, mission);
    }

    @DeleteMapping("/missions/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        return companyService.deleteMission(id);
    }

    @PostMapping("/missions/{id}/publier")
    public ResponseEntity<MissionResponse> publierMission(@PathVariable Long id) {
        return companyService.publierMission(id);
    }

    @PostMapping("/missions/{id}/demarrer")
    public ResponseEntity<MissionResponse> demarrerMission(@PathVariable Long id) {
        return companyService.demarrerMission(id);
    }

    @PostMapping("/missions/{id}/cloturer")
    public ResponseEntity<MissionResponse> cloturerMission(@PathVariable Long id) {
        return companyService.cloturerMission(id);
    }
}