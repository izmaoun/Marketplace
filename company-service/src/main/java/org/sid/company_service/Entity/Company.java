package org.sid.company_service.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sid.company_service.Entity.CompanyStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keycloak user ID — lié au compte d'authentification
    // Récupéré automatiquement depuis jwt.getSubject()
    @Column(unique = true, nullable = false)
    private String keycloakId;

    @Column(unique = true, nullable = false)
    private String companyEmail;

    @Column(nullable = false)
    private String companyName;

    private String companyAddress;
    private String companyPhone;
    private String companyFax;
    private String domaine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.Pending;  // PENDING par défaut

    private String rejectionReason;  // motif si rejeté par l'admin

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}