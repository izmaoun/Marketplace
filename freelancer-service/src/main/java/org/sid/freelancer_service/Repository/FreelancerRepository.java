package org.sid.freelancer_service.Repository;

import org.sid.freelancer_service.Entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
    // Recherche par keycloakId
    Optional<Freelancer> findByKeycloakId(String keycloakId);
}
