package org.sid.freelancer_service.Repository;

import org.sid.freelancer_service.Entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
}

