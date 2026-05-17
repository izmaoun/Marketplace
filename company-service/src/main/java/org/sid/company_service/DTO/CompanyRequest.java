package org.sid.company_service.DTO;

import lombok.Data;

@Data
public class CompanyRequest {
    private String keycloakUserId;     // → Company.keycloakId
    private String email;              // → Company.companyEmail
    private String companyName;        // → Company.companyName
    private String siret;              // → Company.siret
    private String contactFirstName;   // → Company.contactFirstName
    private String contactLastName;    // → Company.contactLastName
    private String status;             // ignoré — toujours forcé à Pending
}