package org.sid.company_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyPublicDTO {
    private Long id;
    private String companyName;
    private String domaine;
    // pas de keycloakId, pas de rejectionReason, pas de status
}