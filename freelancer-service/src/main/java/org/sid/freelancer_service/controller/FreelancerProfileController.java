package org.sid.freelancer_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.freelancer_service.dto.*;
import org.sid.freelancer_service.entity.CvDocument;
import org.sid.freelancer_service.service.FreelancerProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class FreelancerProfileController {

    private final FreelancerProfileService profileService;

    // ──────────────────────────────────────────────────────────────
    // Profile endpoints
    // ──────────────────────────────────────────────────────────────

    /**
     * Create the authenticated user's freelancer profile.
     */
    @PostMapping
    public ResponseEntity<FreelancerProfileResponse> createProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FreelancerProfileRequest request) {
        FreelancerProfileResponse response = profileService.createProfile(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get the authenticated user's own profile.
     */
    @GetMapping("/me")
    public ResponseEntity<FreelancerProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getProfile(jwt.getSubject()));
    }

    /**
     * Get any freelancer's public profile (accessible by companies).
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<FreelancerProfileResponse> getPublicProfile(@PathVariable String profileId) {
        return ResponseEntity.ok(profileService.getProfile(profileId));
    }

    /**
     * Update the authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<FreelancerProfileResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FreelancerProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(jwt.getSubject(), request));
    }

    /**
     * Delete the authenticated user's profile.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        profileService.deleteProfile(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // Skills
    // ──────────────────────────────────────────────────────────────

    @PostMapping("/me/skills")
    public ResponseEntity<SkillResponse> addSkill(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addSkill(jwt.getSubject(), request));
    }

    @PutMapping("/me/skills/{skillId}")
    public ResponseEntity<SkillResponse> updateSkill(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(profileService.updateSkill(jwt.getSubject(), skillId, request));
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long skillId) {
        profileService.deleteSkill(jwt.getSubject(), skillId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // Experiences
    // ──────────────────────────────────────────────────────────────

    @PostMapping("/me/experiences")
    public ResponseEntity<ExperienceResponse> addExperience(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addExperience(jwt.getSubject(), request));
    }

    @PutMapping("/me/experiences/{experienceId}")
    public ResponseEntity<ExperienceResponse> updateExperience(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(profileService.updateExperience(jwt.getSubject(), experienceId, request));
    }

    @DeleteMapping("/me/experiences/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long experienceId) {
        profileService.deleteExperience(jwt.getSubject(), experienceId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // Projects
    // ──────────────────────────────────────────────────────────────

    @PostMapping("/me/projects")
    public ResponseEntity<ProjectResponse> addProject(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addProject(jwt.getSubject(), request));
    }

    @PutMapping("/me/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(profileService.updateProject(jwt.getSubject(), projectId, request));
    }

    @DeleteMapping("/me/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId) {
        profileService.deleteProject(jwt.getSubject(), projectId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // CV Upload / Download / Delete
    // ──────────────────────────────────────────────────────────────

    /**
     * Upload or replace the CV for the authenticated freelancer.
     * Accepts PDF and Word documents up to 10 MB.
     */
    @PostMapping(value = "/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvMetadataResponse> uploadCv(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.uploadCv(jwt.getSubject(), file));
    }

    /**
     * Download a freelancer's CV (publicly accessible).
     */
    @GetMapping("/{profileId}/cv")
    public ResponseEntity<byte[]> downloadCv(@PathVariable String profileId) {
        CvDocument cv = profileService.getCv(profileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + cv.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(cv.getContentType()))
                .contentLength(cv.getFileSize())
                .body(cv.getContent());
    }

    /**
     * Delete the CV for the authenticated freelancer.
     */
    @DeleteMapping("/me/cv")
    public ResponseEntity<Void> deleteCv(@AuthenticationPrincipal Jwt jwt) {
        profileService.deleteCv(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
