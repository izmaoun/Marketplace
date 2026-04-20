package org.sid.freelancer_service.repository;

import org.sid.freelancer_service.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByIdAndProfileId(Long id, String profileId);
}
