package org.sid.auth_service.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "freelancer-service", url = "${services.freelancer.url:http://freelancer-service:8082}")
public interface FreelancerServiceClient {

    @PostMapping("/api/freelances")
    ResponseEntity<Map<String, Object>> createFreelancer(@RequestBody Map<String, Object> freelancerData);
}
