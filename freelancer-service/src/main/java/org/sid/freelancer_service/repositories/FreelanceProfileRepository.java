package org.sid.freelancer_service.repositories;

import org.sid.freelancer_service.entities.FreelanceProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelanceProfileRepository extends JpaRepository<FreelanceProfile, Long> {
}

