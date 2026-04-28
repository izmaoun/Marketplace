package org.sid.freelancer_service.Controller;

import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Service.FreelancerService;
import org.sid.freelancer_service.Service.dto.MissionRequest;
import org.sid.freelancer_service.Service.dto.MissionResponse;
import org.sid.freelancer_service.Service.dto.WorkMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelances")
public class FreelancerController {
    private final FreelancerService service;

    public FreelancerController(FreelancerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Freelancer> getAllProfiles() {
        return service.getAllProfiles();
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Freelancer> getProfile(@PathVariable Long id) {
        return service.getProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Freelancer createProfile(@RequestBody Freelancer profile) {
        return service.saveProfile(profile);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Freelancer> updateProfile(@PathVariable Long id, @RequestBody Freelancer profile) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        profile.setId(id);
        return ResponseEntity.ok(service.saveProfile(profile));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/suspend")
    public ResponseEntity<Void> suspendProfile(@PathVariable Long id) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.suspendProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/missions")
    public ResponseEntity<List<MissionResponse>> getAllMissions() {
        return ResponseEntity.ok(service.getAllMissions());
    }

    @GetMapping("/missions/{id}")
    public ResponseEntity<MissionResponse> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMissionById(id));
    }

    @GetMapping("/missions/publiees")
    public ResponseEntity<List<MissionResponse>> getMissionsPublished() {
        return ResponseEntity.ok(service.getMissionsPublished());
    }

    @GetMapping("/missions/search")
    public ResponseEntity<List<MissionResponse>> searchMissions(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkMode workMode) {
        return ResponseEntity.ok(service.searchMissions(skill, keyword, workMode));
    }

    @GetMapping("/missions/company/{companyId}")
    public ResponseEntity<List<MissionResponse>> getMissionsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.getMissionsByCompany(companyId));
    }

//    @PostMapping("/missions")
//    public ResponseEntity<MissionResponse> createMission(@RequestBody MissionRequest mission) {
//        return ResponseEntity.ok(service.createMission(mission));
//    }
}
