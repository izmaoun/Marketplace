package org.sid.mission_service.services;

import org.sid.mission_service.entities.Mission;
import org.sid.mission_service.entities.MissionStatus;
import org.sid.mission_service.entities.WorkMode;
import org.sid.mission_service.repositories.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    // ── CRUD de base ──────────────────────────────────────────────────────────

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Optional<Mission> getMissionById(Long id) {
        return missionRepository.findById(id);
    }

    public Mission createMission(Mission mission) {
        mission.setStatus(MissionStatus.BROUILLON);
        return missionRepository.save(mission);
    }

    public Optional<Mission> updateMission(Long id, Mission updated) {
        return missionRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setRequiredSkills(updated.getRequiredSkills());
            existing.setDurationDays(updated.getDurationDays());
            existing.setBudget(updated.getBudget());
            existing.setWorkMode(updated.getWorkMode());
            return missionRepository.save(existing);
        });
    }

    public boolean deleteMission(Long id) {
        if (!missionRepository.existsById(id)) return false;
        missionRepository.deleteById(id);
        return true;
    }

    // ── Gestion des statuts ───────────────────────────────────────────────────

    public Optional<Mission> publierMission(Long id) {
        return changerStatut(id, MissionStatus.PUBLIEE);
    }

    public Optional<Mission> demarrerMission(Long id) {
        return changerStatut(id, MissionStatus.EN_COURS);
    }

    public Optional<Mission> cloturerMission(Long id) {
        return changerStatut(id, MissionStatus.CLOTUREE);
    }

    private Optional<Mission> changerStatut(Long id, MissionStatus nouveauStatut) {
        return missionRepository.findById(id).map(mission -> {
            mission.setStatus(nouveauStatut);
            return missionRepository.save(mission);
        });
    }

    // ── Requêtes métier ───────────────────────────────────────────────────────

    public List<Mission> getMissionsByCompany(Long companyId) {
        return missionRepository.findByCompanyId(companyId);
    }

    public List<Mission> getMissionsPublished() {
        return missionRepository.findByStatus(MissionStatus.PUBLIEE);
    }

    public List<Mission> getMissionsBySkill(String skill) {
        return missionRepository.findPublishedBySkill(skill);
    }

    public List<Mission> getMissionsBYWorkMode(WorkMode workMode) {
        return missionRepository.findByStatusAndWorkMode(MissionStatus.PUBLIEE, workMode);
    }

    public List<Mission> searchMissions(String keyword) {
        return missionRepository.searchPublished(keyword);
    }
}
