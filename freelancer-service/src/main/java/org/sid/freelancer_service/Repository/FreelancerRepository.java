package org.sid.freelancer_service.Repository;

import org.sid.freelancer_service.Entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
        boolean existsByEmail(String email);
        boolean existsByKeycloakUserId(String keycloakUserId);
        Freelancer findByKeycloakUserId(String keycloakUserId);
}

