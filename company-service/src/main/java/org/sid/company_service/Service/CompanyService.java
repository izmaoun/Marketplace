package org.sid.company_service.Service;

import org.sid.company_service.Entity.Company;
import org.sid.company_service.Entity.CompanyStatus;
import org.sid.company_service.Repository.CompanyServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import org.sid.company_service.Service.dto.MissionRequest;
import org.sid.company_service.Service.dto.MissionResponse;
import org.springframework.http.ResponseEntity;

@Service
public class CompanyService {

    private final CompanyServiceRepository companyRep;
    private final MissionServiceClient missionServiceClient;

    public CompanyService(CompanyServiceRepository companyRep, MissionServiceClient missionServiceClient) {
        this.companyRep = companyRep;
        this.missionServiceClient = missionServiceClient;
    }



    // ── Récupérer toutes les entreprises ──
    public List<Company> getAllCompanies() {
        return companyRep.findAll();
    }

    // ── Récupérer une entreprise par ID ──
    public Company getCompanyById(Long id) {
        return companyRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée : " + id));
    }

    // ── Récupérer l'entreprise d'un utilisateur connecté ──
    public Company getCompanyByKeycloakId(String keycloakId) {
        return companyRep.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Aucune entreprise pour cet utilisateur"));
    }

    // ── Créer une entreprise (appelé à l'inscription) ──
    public Company saveCompany(Company company) {
        company.setStatus(CompanyStatus.Pending); // toujours PENDING à la création
        return companyRep.save(company);
    }

    // ── Modifier son profil entreprise ──
    public Company updateCompany(Long id, Company updated) {
        Company existing = getCompanyById(id);
        existing.setCompanyName(updated.getCompanyName());
        existing.setCompanyAddress(updated.getCompanyAddress());
        existing.setCompanyPhone(updated.getCompanyPhone());
        existing.setCompanyFax(updated.getCompanyFax());
        existing.setDomaine(updated.getDomaine());
        return companyRep.save(existing);
    }

    // ── Supprimer une entreprise ──
    public void deleteCompany(Long id) {
        companyRep.deleteById(id);
    }

    // ── ADMIN : liste des entreprises en attente ──
    public List<Company> getPendingCompanies() {
        return companyRep.findByStatus(CompanyStatus.Pending);
    }

    // ── ADMIN : valider une entreprise ──
    public Company validateCompany(Long id) {
        Company company = getCompanyById(id);
        company.setStatus(CompanyStatus.Validated);
        company.setRejectionReason(null);
        return companyRep.save(company);
    }

    // ── ADMIN : rejeter une entreprise ──
    public Company rejectCompany(Long id, String reason) {
        Company company = getCompanyById(id);
        company.setStatus(CompanyStatus.Rejected);
        company.setRejectionReason(reason);
        return companyRep.save(company);
    }

    // ── Vérifier qu'une entreprise est validée ──
    // Appelé par mission-service avant de créer une mission
    public boolean isValidated(String keycloakId) {
        return companyRep.findByKeycloakId(keycloakId)
                .map(c -> c.getStatus() == CompanyStatus.Validated)
                .orElse(false);
    }

    public MissionResponse createMission(MissionRequest mission) {
        return missionServiceClient.createMission(mission);
    }

    public ResponseEntity<MissionResponse> updateMission(Long id, MissionRequest mission) {
        return missionServiceClient.updateMission(id, mission);
    }

    public ResponseEntity<Void> deleteMission(Long id) {
        return missionServiceClient.deleteMission(id);
    }

    public ResponseEntity<MissionResponse> publierMission(Long id) {
        return missionServiceClient.publierMission(id);
    }

    public ResponseEntity<MissionResponse> demarrerMission(Long id) {
        return missionServiceClient.demarrerMission(id);
    }

    public ResponseEntity<MissionResponse> cloturerMission(Long id) {
        return missionServiceClient.cloturerMission(id);
    }
}