package org.sid.freelancer_service.Service;

import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Repository.FreelancerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.sid.freelancer_service.Service.dto.MissionRequest;
import org.sid.freelancer_service.Service.dto.MissionResponse;
import org.sid.freelancer_service.Service.dto.WorkMode;

@Service
public class FreelancerService {
    private final FreelancerRepository repository;
    private final MissionServiceClient missionServiceClient;

    public FreelancerService(FreelancerRepository repository, MissionServiceClient missionServiceClient) {
        this.repository = repository;
        this.missionServiceClient = missionServiceClient;
    }

    public List<Freelancer> getAllProfiles() {
        return repository.findAll();
    }

    public Optional<Freelancer> getProfile(Long id) {
        return repository.findById(id);
    }

    public Freelancer saveProfile(Freelancer profile) {
        return repository.save(profile);
    }

    public void deleteProfile(Long id) {
        repository.deleteById(id);
    }

    public void suspendProfile(Long id) {
        repository.findById(id).ifPresent(profile -> {
            profile.setSuspended(true);
            repository.save(profile);
        });
    }

    public List<MissionResponse> getAllMissions() {
        return missionServiceClient.getAllMissions();
    }

    public MissionResponse getMissionById(Long id) {
        return missionServiceClient.getMissionById(id);
    }

    public List<MissionResponse> getMissionsPublished() {
        return missionServiceClient.getMissionsPublished();
    }

    public List<MissionResponse> searchMissions(String skill, String keyword, WorkMode workMode) {
        return missionServiceClient.searchMissions(skill, keyword, workMode);
    }

    public List<MissionResponse> getMissionsByCompany(Long companyId) {
        return missionServiceClient.getMissionsByCompany(companyId);
    }

//    public MissionResponse createMission(MissionRequest mission) {
//        return missionServiceClient.createMission(mission);
//    }
}
