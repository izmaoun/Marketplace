package org.sid.company_service.Controller;

import lombok.RequiredArgsConstructor;
import org.sid.company_service.Entity.Company;
import org.sid.company_service.Service.CompanyService;
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
}