package org.sid.application_service.service;

import feign.FeignException;
import org.sid.application_service.client.CompanyServiceClient;
import org.sid.application_service.client.FreelancerServiceClient;
import org.sid.application_service.client.MissionServiceClient;
import org.sid.application_service.dto.ApplicationRequest;
import org.sid.application_service.dto.ApplicationResponse;
import org.sid.application_service.dto.ApplicationStatusRequest;
import org.sid.application_service.dto.CompanyResponse;
import org.sid.application_service.dto.FreelancerProfileDTO;
import org.sid.application_service.dto.MissionResponse;
import org.sid.application_service.entity.Application;
import org.sid.application_service.entity.ApplicationStatus;
import org.sid.application_service.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository repository;
    private final MissionServiceClient missionServiceClient;
    private final FreelancerServiceClient freelancerServiceClient;
    private final CompanyServiceClient companyServiceClient;

    public ApplicationService(ApplicationRepository repository,
                              MissionServiceClient missionServiceClient,
                              FreelancerServiceClient freelancerServiceClient,
                              CompanyServiceClient companyServiceClient) {
        this.repository = repository;
        this.missionServiceClient = missionServiceClient;
        this.freelancerServiceClient = freelancerServiceClient;
        this.companyServiceClient = companyServiceClient;
    }

    public ApplicationResponse apply(Jwt jwt, ApplicationRequest request) {
        if (request.getMissionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missionId is required");
        }

        MissionResponse mission = getPublishedMission(request.getMissionId());
        FreelancerProfileDTO freelancer = getCurrentFreelancer();
        String freelancerKeycloakId = jwt.getSubject();

        if (repository.existsByMissionIdAndFreelancerKeycloakId(mission.getId(), freelancerKeycloakId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Freelancer already applied to this mission");
        }

        Application application = Application.builder()
                .missionId(mission.getId())
                .missionCompanyId(mission.getCompanyId())
                .freelancerKeycloakId(freelancerKeycloakId)
                .freelancerFullname(freelancer.displayName())
                .coverLetter(request.getCoverLetter())
                .compatibilityScore(calculateCompatibilityScore(freelancer, mission))
                .status(ApplicationStatus.PENDING)
                .build();

        return toResponse(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(Jwt jwt) {
        return repository.findByFreelancerKeycloakIdOrderByCreatedAtDesc(jwt.getSubject())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id, Jwt jwt) {
        Application application = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (hasRole(jwt, "ADMIN")
                || application.getFreelancerKeycloakId().equals(jwt.getSubject())
                || (hasRole(jwt, "COMPANY") && ownsMission(application.getMissionCompanyId()))) {
            return toResponse(application);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this application");
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForMission(Long missionId) {
        MissionResponse mission = getPublishedMission(missionId);
        assertCurrentCompanyOwns(mission.getCompanyId());

        return repository.findByMissionIdOrderByCompatibilityScoreDescCreatedAtAsc(missionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForCurrentCompany() {
        CompanyResponse company = companyServiceClient.getMyCompany();
        return repository.findByMissionCompanyIdOrderByCreatedAtDesc(company.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAcceptedApplicationsForMission(Long missionId) {
        MissionResponse mission = getPublishedMission(missionId);
        assertCurrentCompanyOwns(mission.getCompanyId());

        return repository.findByMissionIdAndStatusOrderByCompatibilityScoreDescCreatedAtAsc(
                        missionId,
                        ApplicationStatus.ACCEPTED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse updateStatus(Long id, ApplicationStatusRequest request) {
        if (request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        if (request.getStatus() == ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use ACCEPTED, REJECTED or WAITLISTED");
        }

        Application application = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        assertCurrentCompanyOwns(application.getMissionCompanyId());

        application.setStatus(request.getStatus());
        return toResponse(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplicationsForAdmin() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private MissionResponse getPublishedMission(Long missionId) {
        try {
            MissionResponse mission = missionServiceClient.getMissionById(missionId);
            if (mission == null || !"PUBLIEE".equalsIgnoreCase(mission.getStatus())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Published mission not found");
            }
            return mission;
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Published mission not found");
        }
    }

    private FreelancerProfileDTO getCurrentFreelancer() {
        try {
            return freelancerServiceClient.getMyProfile();
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Freelancer profile not found");
        }
    }

    private void assertCurrentCompanyOwns(Long companyId) {
        if (!ownsMission(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mission does not belong to current company");
        }
    }

    private boolean ownsMission(Long companyId) {
        CompanyResponse company = getCurrentCompany();
        return company != null && company.getId() != null && company.getId().equals(companyId);
    }

    private CompanyResponse getCurrentCompany() {
        try {
            return companyServiceClient.getMyCompany();
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company profile not found");
        }
    }

    private int calculateCompatibilityScore(FreelancerProfileDTO freelancer, MissionResponse mission) {
        Set<String> freelancerSkills = normalize(freelancer.getSkills());
        Set<String> requiredSkills = normalize(mission.getRequiredSkills());
        if (requiredSkills.isEmpty()) {
            return 50;
        }

        long matched = requiredSkills.stream()
                .filter(freelancerSkills::contains)
                .count();
        int score = (int) Math.round((matched * 100.0) / requiredSkills.size());

        String summary = freelancer.getSummary() == null ? "" : freelancer.getSummary().toLowerCase(Locale.ROOT);
        for (String skill : requiredSkills) {
            if (summary.contains(skill)) {
                score = Math.min(100, score + 5);
            }
        }
        return score;
    }

    private Set<String> normalize(Collection<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        Set<String> normalized = new HashSet<>();
        values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .forEach(normalized::add);
        return normalized;
    }

    private boolean hasRole(Jwt jwt, String role) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return false;
        }
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return false;
        }
        return roles.stream().anyMatch(item -> role.equals(item));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getMissionId(),
                application.getMissionCompanyId(),
                application.getFreelancerFullname(),
                application.getCoverLetter(),
                application.getCompatibilityScore(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
