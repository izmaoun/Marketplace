package org.sid.freelancer_service.repository;

import org.sid.freelancer_service.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    Optional<Experience> findByIdAndProfileId(Long id, String profileId);
}
