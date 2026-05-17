package org.sid.freelancer_service.Service;

import org.sid.freelancer_service.DTO.FreelancerRequest;
import org.sid.freelancer_service.DTO.FreelancerResponse;
import org.sid.freelancer_service.DTO.MissionResponse;
import org.sid.freelancer_service.DTO.WorkMode;
import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Repository.FreelancerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FreelancerService {

    private final FreelancerRepository repository;
    private final MissionServiceClient missionServiceClient;

    private static final int MAX_PAGE_SIZE = 100;

    public FreelancerService(FreelancerRepository repository,
                             MissionServiceClient missionServiceClient) {
        this.repository = repository;
        this.missionServiceClient = missionServiceClient;
    }

    // ✅ Remplace getAllProfiles() — paginé, trié par lastName
    public Page<Freelancer> getAllProfiles(int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return repository.findAll(
                PageRequest.of(page, safeSize, Sort.by("lastName").ascending())
        );
    }

    public Freelancer findByKeycloakUserId(String keycloakUserId) {
        return repository.findByKeycloakUserId(keycloakUserId);
    }

    public Optional<Freelancer> getProfile(Long id) {
        return repository.findById(id);
    }

    public Freelancer saveProfile(Freelancer profile) {
        return repository.save(profile);
    }

    @Transactional
    public FreelancerResponse createFreelancer(FreelancerRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        }
        if (repository.existsByKeycloakUserId(request.getKeycloakUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Keycloak user already linked");
        }

        Freelancer freelancer = new Freelancer();
        freelancer.setKeycloakUserId(request.getKeycloakUserId());
        freelancer.setEmail(request.getEmail());
        freelancer.setFirstName(request.getFirstName());
        freelancer.setLastName(request.getLastName());
        freelancer.setPhone(request.getPhone());
        freelancer.setSummary(request.getSummary());
        freelancer.setCvUrl(request.getCvUrl());
        freelancer.setPfpUrl(request.getPfpUrl());
        freelancer.setSkills(new ArrayList<>());
        freelancer.setExperiences(new ArrayList<>());
        freelancer.setProjects(new ArrayList<>());

        Freelancer saved = repository.save(freelancer);

        FreelancerResponse response = new FreelancerResponse();
        response.setId(saved.getId());
        response.setKeycloakUserId(saved.getKeycloakUserId());
        response.setEmail(saved.getEmail());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        return response;
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
}