package org.sid.mission_service.repositories;

import org.sid.mission_service.entities.Mission;
import org.sid.mission_service.entities.MissionStatus;
import org.sid.mission_service.entities.WorkMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    // Toutes les missions d'une entreprise
    List<Mission> findByCompanyId(Long companyId);

    // Missions par statut
    List<Mission> findByStatus(MissionStatus status);

    // Missions publiées filtrables par compétence et mode de travail
    @Query("SELECT m FROM Mission m JOIN m.requiredSkills s " +
           "WHERE m.status = 'PUBLIEE' AND s = :skill")
    List<Mission> findPublishedBySkill(@Param("skill") String skill);

    List<Mission> findByStatusAndWorkMode(MissionStatus status, WorkMode workMode);

    // Recherche fulltext simple sur titre et description
    @Query("SELECT m FROM Mission m WHERE m.status = 'PUBLIEE' AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Mission> searchPublished(@Param("keyword") String keyword);
}
