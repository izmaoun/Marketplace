package org.sid.application_service.client;

import org.sid.application_service.dto.MissionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mission-service", url = "${services.mission.url}")
public interface MissionServiceClient {
    @GetMapping("/api/missions/{id}")
    MissionResponse getMissionById(@PathVariable("id") Long id);
}
