package org.sid.auth_service.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "company-service", url = "${services.company.url:http://company-service:8083}")
public interface CompanyServiceClient {

    @PostMapping("/api/companies")
    ResponseEntity<Map<String, Object>> createCompany(@RequestBody Map<String, Object> companyData);
}
