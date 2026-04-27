package org.sid.freelancer_service.Controller;

import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Service.FreelancerService;
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

    @GetMapping("/{id}")
    public ResponseEntity<Freelancer> getProfile(@PathVariable Long id) {
        return service.getProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Freelancer createProfile(@RequestBody Freelancer profile) {
        return service.saveProfile(profile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Freelancer> updateProfile(@PathVariable Long id, @RequestBody Freelancer profile) {
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

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendProfile(@PathVariable Long id) {
        if (!service.getProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.suspendProfile(id);
        return ResponseEntity.noContent().build();
    }
}
