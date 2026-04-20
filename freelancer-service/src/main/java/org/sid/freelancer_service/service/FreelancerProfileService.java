package org.sid.freelancer_service.service;

import lombok.RequiredArgsConstructor;
import org.sid.freelancer_service.dto.*;
import org.sid.freelancer_service.entity.*;
import org.sid.freelancer_service.exception.ResourceNotFoundException;
import org.sid.freelancer_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class FreelancerProfileService {

    private static final Set<String> ALLOWED_CV_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final FreelancerProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final CvDocumentRepository cvDocumentRepository;

    // ──────────────────────────────────────────────────────────────
    // Profile CRUD
    // ──────────────────────────────────────────────────────────────

    public FreelancerProfileResponse createProfile(String userId, FreelancerProfileRequest request) {
        if (profileRepository.existsById(userId)) {
            throw new IllegalArgumentException("A profile already exists for this user.");
        }
        FreelancerProfile profile = FreelancerProfile.builder()
                .id(userId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .title(request.title())
                .bio(request.bio())
                .location(request.location())
                .linkedinUrl(request.linkedinUrl())
                .githubUrl(request.githubUrl())
                .portfolioUrl(request.portfolioUrl())
                .availableForWork(request.availableForWork())
                .hourlyRate(request.hourlyRate())
                .build();
        return toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getProfile(String profileId) {
        FreelancerProfile profile = findProfileOrThrow(profileId);
        return toResponse(profile);
    }

    public FreelancerProfileResponse updateProfile(String userId, FreelancerProfileRequest request) {
        FreelancerProfile profile = findProfileOrThrow(userId);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());
        profile.setTitle(request.title());
        profile.setBio(request.bio());
        profile.setLocation(request.location());
        profile.setLinkedinUrl(request.linkedinUrl());
        profile.setGithubUrl(request.githubUrl());
        profile.setPortfolioUrl(request.portfolioUrl());
        profile.setAvailableForWork(request.availableForWork());
        profile.setHourlyRate(request.hourlyRate());
        return toResponse(profileRepository.save(profile));
    }

    public void deleteProfile(String userId) {
        FreelancerProfile profile = findProfileOrThrow(userId);
        profileRepository.delete(profile);
    }

    // ──────────────────────────────────────────────────────────────
    // Skills
    // ──────────────────────────────────────────────────────────────

    public SkillResponse addSkill(String userId, SkillRequest request) {
        FreelancerProfile profile = findProfileOrThrow(userId);
        Skill skill = Skill.builder()
                .name(request.name())
                .level(request.level())
                .yearsOfExperience(request.yearsOfExperience())
                .profile(profile)
                .build();
        return toSkillResponse(skillRepository.save(skill));
    }

    public SkillResponse updateSkill(String userId, Long skillId, SkillRequest request) {
        Skill skill = skillRepository.findByIdAndProfileId(skillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        skill.setName(request.name());
        skill.setLevel(request.level());
        skill.setYearsOfExperience(request.yearsOfExperience());
        return toSkillResponse(skillRepository.save(skill));
    }

    public void deleteSkill(String userId, Long skillId) {
        Skill skill = skillRepository.findByIdAndProfileId(skillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        skillRepository.delete(skill);
    }

    // ──────────────────────────────────────────────────────────────
    // Experiences
    // ──────────────────────────────────────────────────────────────

    public ExperienceResponse addExperience(String userId, ExperienceRequest request) {
        FreelancerProfile profile = findProfileOrThrow(userId);
        Experience experience = Experience.builder()
                .title(request.title())
                .company(request.company())
                .location(request.location())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .current(request.current())
                .description(request.description())
                .profile(profile)
                .build();
        return toExperienceResponse(experienceRepository.save(experience));
    }

    public ExperienceResponse updateExperience(String userId, Long experienceId, ExperienceRequest request) {
        Experience experience = experienceRepository.findByIdAndProfileId(experienceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id: " + experienceId));
        experience.setTitle(request.title());
        experience.setCompany(request.company());
        experience.setLocation(request.location());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCurrent(request.current());
        experience.setDescription(request.description());
        return toExperienceResponse(experienceRepository.save(experience));
    }

    public void deleteExperience(String userId, Long experienceId) {
        Experience experience = experienceRepository.findByIdAndProfileId(experienceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id: " + experienceId));
        experienceRepository.delete(experience);
    }

    // ──────────────────────────────────────────────────────────────
    // Projects
    // ──────────────────────────────────────────────────────────────

    public ProjectResponse addProject(String userId, ProjectRequest request) {
        FreelancerProfile profile = findProfileOrThrow(userId);
        Project project = Project.builder()
                .title(request.title())
                .description(request.description())
                .technologies(request.technologies())
                .projectUrl(request.projectUrl())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .profile(profile)
                .build();
        return toProjectResponse(projectRepository.save(project));
    }

    public ProjectResponse updateProject(String userId, Long projectId, ProjectRequest request) {
        Project project = projectRepository.findByIdAndProfileId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setTechnologies(request.technologies());
        project.setProjectUrl(request.projectUrl());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        return toProjectResponse(projectRepository.save(project));
    }

    public void deleteProject(String userId, Long projectId) {
        Project project = projectRepository.findByIdAndProfileId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        projectRepository.delete(project);
    }

    // ──────────────────────────────────────────────────────────────
    // CV Upload / Download / Delete
    // ──────────────────────────────────────────────────────────────

    public CvMetadataResponse uploadCv(String userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CV_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF and Word documents (.pdf, .doc, .docx) are accepted.");
        }
        FreelancerProfile profile = findProfileOrThrow(userId);
        CvDocument cv = cvDocumentRepository.findByProfileId(userId)
                .orElse(CvDocument.builder().profile(profile).build());
        cv.setOriginalFilename(file.getOriginalFilename());
        cv.setContentType(contentType);
        cv.setFileSize(file.getSize());
        cv.setContent(file.getBytes());
        return toCvMetadataResponse(cvDocumentRepository.save(cv));
    }

    @Transactional(readOnly = true)
    public CvDocument getCv(String profileId) {
        return cvDocumentRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("No CV found for profile: " + profileId));
    }

    public void deleteCv(String userId) {
        CvDocument cv = cvDocumentRepository.findByProfileId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No CV found for this profile."));
        cvDocumentRepository.delete(cv);
    }

    // ──────────────────────────────────────────────────────────────
    // Mapping helpers
    // ──────────────────────────────────────────────────────────────

    private FreelancerProfile findProfileOrThrow(String profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));
    }

    private FreelancerProfileResponse toResponse(FreelancerProfile p) {
        List<SkillResponse> skills = p.getSkills().stream().map(this::toSkillResponse).toList();
        List<ExperienceResponse> experiences = p.getExperiences().stream().map(this::toExperienceResponse).toList();
        List<ProjectResponse> projects = p.getProjects().stream().map(this::toProjectResponse).toList();
        return new FreelancerProfileResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getPhone(),
                p.getTitle(),
                p.getBio(),
                p.getLocation(),
                p.getLinkedinUrl(),
                p.getGithubUrl(),
                p.getPortfolioUrl(),
                p.isAvailableForWork(),
                p.getHourlyRate(),
                skills,
                experiences,
                projects,
                p.getCv() != null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private SkillResponse toSkillResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getLevel(), s.getYearsOfExperience());
    }

    private ExperienceResponse toExperienceResponse(Experience e) {
        return new ExperienceResponse(e.getId(), e.getTitle(), e.getCompany(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.isCurrent(), e.getDescription());
    }

    private ProjectResponse toProjectResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getTitle(), p.getDescription(),
                p.getTechnologies(), p.getProjectUrl(), p.getStartDate(), p.getEndDate());
    }

    private CvMetadataResponse toCvMetadataResponse(CvDocument cv) {
        return new CvMetadataResponse(cv.getId(), cv.getOriginalFilename(),
                cv.getContentType(), cv.getFileSize(), cv.getUploadedAt());
    }
}
