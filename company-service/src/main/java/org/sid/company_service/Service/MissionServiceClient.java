package org.sid.company_service.Service;

import org.sid.company_service.Service.dto.MissionRequest;
import org.sid.company_service.Service.dto.MissionResponse;
import org.sid.company_service.Service.dto.WorkMode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mission-service", url = "http://mission-service:8084")
public interface MissionServiceClient {

//    @GetMapping("/api/missions/company/{companyId}")
//    List<MissionResponse> getMissionsByCompany(@PathVariable("companyId") Long companyId);

    @PostMapping("/api/missions")
    MissionResponse createMission(@RequestBody MissionRequest mission);

    @PutMapping("/api/missions/{id}")
    ResponseEntity<MissionResponse> updateMission(
            @PathVariable("id") Long id,
            @RequestBody MissionRequest mission);

    @DeleteMapping("/api/missions/{id}")
    ResponseEntity<Void> deleteMission(@PathVariable("id") Long id);

    @PostMapping("/api/missions/{id}/publier")
    ResponseEntity<MissionResponse> publierMission(@PathVariable("id") Long id);

    @PostMapping("/api/missions/{id}/demarrer")
    ResponseEntity<MissionResponse> demarrerMission(@PathVariable("id") Long id);

    @PostMapping("/api/missions/{id}/cloturer")
    ResponseEntity<MissionResponse> cloturerMission(@PathVariable("id") Long id);

//    @GetMapping("/api/missions/search")
//    List<MissionResponse> searchMissions(
//            @RequestParam(value = "skill", required = false) String skill,
//            @RequestParam(value = "keyword", required = false) String keyword,
//            @RequestParam(value = "workMode", required = false) WorkMode workMode);
}
