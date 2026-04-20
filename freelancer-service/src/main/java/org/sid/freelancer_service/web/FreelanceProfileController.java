package org.sid.freelancer_service.web;

import org.sid.freelancer_service.entities.FreelanceProfile;
import org.sid.freelancer_service.services.FreelanceProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelances")
public class FreelanceProfileController {
    private final FreelanceProfileService service;

    public FreelanceProfileController(FreelanceProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<FreelanceProfile> getAllProfiles() {
        return service.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreelanceProfile> getProfile(@PathVariable Long id) {
        return service.getProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FreelanceProfile createProfile(@RequestBody FreelanceProfile profile) {
        return service.saveProfile(profile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FreelanceProfile> updateProfile(@PathVariable Long id, @RequestBody FreelanceProfile profile) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        profile.setId(id);
        return ResponseEntity.ok(service.saveProfile(profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}

