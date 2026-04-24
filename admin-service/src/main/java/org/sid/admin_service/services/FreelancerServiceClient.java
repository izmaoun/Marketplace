package org.sid.admin_service.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "freelancer-service", url = "http://freelancer-service:8082")
public interface FreelancerServiceClient {

    @PostMapping("/api/freelances/{id}/suspend")
    void suspendFreelancer(@PathVariable("id") Long freelancerId);

    @DeleteMapping("/api/freelances/{id}")
    void deleteFreelancer(@PathVariable("id") Long freelancerId);
}
