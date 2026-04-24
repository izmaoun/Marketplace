package org.sid.mission_service.web;

import org.sid.mission_service.entities.Mission;
import org.sid.mission_service.entities.WorkMode;
import org.sid.mission_service.services.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public List<Mission> getAllMissions() {
        return missionService.getAllMissions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable Long id) {
        return missionService.getMissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mission createMission(@RequestBody Mission mission) {
        return missionService.createMission(mission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mission> updateMission(@PathVariable Long id, @RequestBody Mission mission) {
        return missionService.updateMission(id, mission)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        if (!missionService.deleteMission(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ── Gestion des statuts ───────────────────────────────────────────────────

    @PostMapping("/{id}/publier")
    public ResponseEntity<Mission> publierMission(@PathVariable Long id) {
        return missionService.publierMission(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<Mission> demarrerMission(@PathVariable Long id) {
        return missionService.demarrerMission(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cloturer")
    public ResponseEntity<Mission> cloturerMission(@PathVariable Long id) {
        return missionService.cloturerMission(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Recherche & filtres ───────────────────────────────────────────────────

    // Missions d'une entreprise : GET /api/missions/company/42
    @GetMapping("/company/{companyId}")
    public List<Mission> getMissionsByCompany(@PathVariable Long companyId) {
        return missionService.getMissionsByCompany(companyId);
    }

    // Toutes les missions publiées : GET /api/missions/publiees
    @GetMapping("/publiees")
    public List<Mission> getMissionsPublished() {
        return missionService.getMissionsPublished();
    }

    // Filtrer par compétence : GET /api/missions/search?skill=Java
    @GetMapping("/search")
    public List<Mission> rechercherMissions(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkMode workMode) {

        if (skill != null) {
            return missionService.getMissionsBySkill(skill);
        }
        if (workMode != null) {
            return missionService.getMissionsBYWorkMode(workMode);
        }
        if (keyword != null) {
            return missionService.searchMissions(keyword);
        }
        return missionService.getMissionsPublished();
    }
}
