package org.sid.admin_service.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "application-service", url = "${services.application.url:http://application-service:8085}")
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/admin/all")
    List<Map<String, Object>> getAllApplicationsForAdmin();
}
