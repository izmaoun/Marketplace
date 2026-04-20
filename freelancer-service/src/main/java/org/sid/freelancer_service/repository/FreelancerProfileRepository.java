package org.sid.freelancer_service.repository;

import org.sid.freelancer_service.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, String> {
}
