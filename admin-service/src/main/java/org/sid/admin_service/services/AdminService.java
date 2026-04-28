package org.sid.admin_service.services;

import org.sid.admin_service.entities.Admin;
import org.sid.admin_service.repositories.AdminRepository;
import org.sid.admin_service.entities.AuditLog;
import org.sid.admin_service.repositories.AuditLogRepository;
import org.sid.admin_service.dto.CompanyDto;
import org.sid.admin_service.dto.FreelancerDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final AuditLogRepository auditLogRepository;
    private final FreelancerServiceClient freelancerServiceClient;
    private final CompanyServiceClient companyServiceClient;

    public AdminService(AdminRepository adminRepository, AuditLogRepository auditLogRepository, FreelancerServiceClient freelancerServiceClient, CompanyServiceClient companyServiceClient) {
        this.adminRepository = adminRepository;
        this.auditLogRepository = auditLogRepository;
        this.freelancerServiceClient = freelancerServiceClient;
        this.companyServiceClient=companyServiceClient;
    }

    // Méthodes pour gérer l'administrateur unique
    public Admin getAdmin() {
        Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);
        if (admin == null) {
            // Création par défaut de l'admin unique s'il n'existe pas
            admin = Admin.builder()
                    .username("admin")
                    .password("admin") // Mot de passe par défaut
                    .email("admin@marketplace.com")
                    .build();
            admin = adminRepository.save(admin);
        }
        return admin;
    }

    public Admin updateAdmin(Admin admin) {
        Admin existing = getAdmin();
        if (existing != null) {
            existing.setUsername(admin.getUsername());
            existing.setPassword(admin.getPassword());
            existing.setEmail(admin.getEmail());
            return adminRepository.save(existing);
        }
        return null;
    }

    // Méthodes de gestion des comptes
    public void approveCompany(Long companyId) {
        // TODO: Implémenter la logique d'approbation d'une entreprise (ex: appel à company-service)
        try {
            companyServiceClient.validateCompany(companyId);
            logAction("Approbation d'entreprise", "Company", companyId, null);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors d'approbation d'entreprise: " + e.getMessage());
        }
    }

    public CompanyDto rejectCompany(Long companyId, String reason) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("reason", reason);
            CompanyDto result = companyServiceClient.rejectCompany(companyId, body);
            logAction("Rejet d'entreprise", "Company", companyId, reason);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du reject d'entreprise: " + e.getMessage());
        }
    }

    public List<CompanyDto> getPendingCompanies() {
        try {
            return companyServiceClient.getPendingCompanies();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des entreprises en attente: " + e.getMessage());
        }
    }

    public List<FreelancerDto> getAllFreelancers() {
        try {
            return freelancerServiceClient.getAllFreelancers();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des freelances: " + e.getMessage());
        }
    }

    public void suspendFreelancer(Long freelancerId, String reason) {
        try {
            freelancerServiceClient.suspendFreelancer(freelancerId);
            logAction("Suspension de freelance", "Freelancer", freelancerId, reason);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suspension du freelancer: " + e.getMessage());
        }
    }

    public void deleteCompany(Long companyId, String reason) {
        // TODO: Implémenter la logique de suppression
        logAction("Suppression d'entreprise", "Company", companyId, reason);
    }

    public void deleteFreelancer(Long freelancerId, String reason) {
        try {
            freelancerServiceClient.deleteFreelancer(freelancerId);
            logAction("Suppression de freelance", "Freelancer", freelancerId, reason);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du freelancer: " + e.getMessage());
        }
    }

    private void logAction(String action, String targetType, Long targetId, String reason) {
        // On récupère l'admin unique pour le journal d'audit
        Admin admin = getAdmin();
        String adminUsername = admin != null ? admin.getUsername() : "admin";

        AuditLog auditLog = AuditLog.builder()
                .adminUsername(adminUsername)
                .action(action)
                .targetAccountType(targetType)
                .targetAccountId(targetId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}
