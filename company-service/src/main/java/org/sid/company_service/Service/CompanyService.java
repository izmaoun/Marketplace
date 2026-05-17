package org.sid.company_service.Service;

import org.sid.company_service.DTO.CompanyMeDTO;
import org.sid.company_service.DTO.CompanyRequest;
import org.sid.company_service.DTO.CompanyResponse;
import org.sid.company_service.DTO.MissionRequest;
import org.sid.company_service.DTO.MissionResponse;
import org.sid.company_service.Entity.Company;
import org.sid.company_service.Entity.CompanyStatus;
import org.sid.company_service.Repository.CompanyServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyServiceRepository companyRep;
    private final MissionServiceClient missionServiceClient;

    public CompanyService(CompanyServiceRepository companyRep,
                          MissionServiceClient missionServiceClient) {
        this.companyRep = companyRep;
        this.missionServiceClient = missionServiceClient;
    }

    // ── Appelé par auth-service à l'inscription ──────────────────────────────
    public CompanyResponse saveCompanyFromAuth(CompanyRequest request) {
        // Vérification unicité email et siret
        if (companyRep.findByCompanyEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        Company company = Company.builder()
                .keycloakId(request.getKeycloakUserId())
                .companyEmail(request.getEmail())
                .companyName(request.getCompanyName())
                .siret(request.getSiret())
                .contactFirstName(request.getContactFirstName())
                .contactLastName(request.getContactLastName())
                .status(CompanyStatus.Pending) // toujours forcé à Pending
                .build();

        Company saved = companyRep.save(company);
        return new CompanyResponse(saved.getId(), saved.getCompanyName(), saved.getStatus().name());
    }

    // ── Récupérer toutes les entreprises ─────────────────────────────────────
    public List<Company> getAllCompanies() {
        return companyRep.findAll();
    }

    // ── Récupérer une entreprise par ID ──────────────────────────────────────
    public Company getCompanyById(Long id) {
        return companyRep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entreprise non trouvée"));
    }

    // ── Récupérer l'entreprise du user connecté ───────────────────────────────
    public Company getCompanyByKeycloakId(String keycloakId) {
        return companyRep.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucune entreprise pour cet utilisateur"));
    }

    // ── Modifier le profil entreprise ────────────────────────────────────────
    public Company updateCompany(Long id, CompanyMeDTO updated) {
        Company existing = getCompanyById(id);
        existing.setCompanyName(updated.getCompanyName());
        existing.setCompanyAddress(updated.getCompanyAddress());
        existing.setCompanyPhone(updated.getCompanyPhone());
        existing.setCompanyFax(updated.getCompanyFax());
        existing.setDomaine(updated.getDomaine());
        return companyRep.save(existing);
    }

    public Company saveCompany(CompanyRequest request) {
        Company company = new Company();
        company.setKeycloakId(request.getKeycloakUserId());
        company.setCompanyEmail(request.getEmail());
        company.setCompanyName(request.getCompanyName());
        company.setStatus(CompanyStatus.Pending); // toujours PENDING, jamais depuis le client
        return companyRep.save(company);
    }

    // ── Supprimer une entreprise ─────────────────────────────────────────────
    public void deleteCompany(Long id) {
        companyRep.deleteById(id);
    }

    // ── ADMIN : liste des entreprises en attente ──────────────────────────────
    public List<Company> getPendingCompanies() {
        return companyRep.findByStatus(CompanyStatus.Pending);
    }

    // ── ADMIN : valider ───────────────────────────────────────────────────────
    public Company validateCompany(Long id) {
        Company company = getCompanyById(id);
        company.setStatus(CompanyStatus.Validated);
        company.setRejectionReason(null);
        return companyRep.save(company);
    }

    // ── ADMIN : rejeter ───────────────────────────────────────────────────────
    public Company rejectCompany(Long id, String reason) {
        Company company = getCompanyById(id);
        company.setStatus(CompanyStatus.Rejected);
        company.setRejectionReason(reason);
        return companyRep.save(company);
    }

    // ── Missions ──────────────────────────────────────────────────────────────
    public MissionResponse createMission(String keycloakId, MissionRequest mission) {
        Company company = getCompanyByKeycloakId(keycloakId);
        if (company.getStatus() != CompanyStatus.Validated) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company not validated");
        }
        mission.setCompanyId(company.getId());
        return missionServiceClient.createMission(mission);
    }

    public ResponseEntity<MissionResponse> updateMission(String keycloakId, Long id, MissionRequest mission) {
        Company company = getCompanyByKeycloakId(keycloakId);
        mission.setCompanyId(company.getId());
        return missionServiceClient.updateMissionForCompany(id, company.getId(), mission);
    }

    public ResponseEntity<Void> deleteMission(String keycloakId, Long id) {
        Company company = getCompanyByKeycloakId(keycloakId);
        return missionServiceClient.deleteMissionForCompany(id, company.getId());
    }

    public ResponseEntity<MissionResponse> publierMission(String keycloakId, Long id) {
        Company company = getCompanyByKeycloakId(keycloakId);
        return missionServiceClient.publierMissionForCompany(id, company.getId());
    }

    public ResponseEntity<MissionResponse> demarrerMission(String keycloakId, Long id) {
        Company company = getCompanyByKeycloakId(keycloakId);
        return missionServiceClient.demarrerMissionForCompany(id, company.getId());
    }

    public ResponseEntity<MissionResponse> cloturerMission(String keycloakId, Long id) {
        Company company = getCompanyByKeycloakId(keycloakId);
        return missionServiceClient.cloturerMissionForCompany(id, company.getId());
    }
}
