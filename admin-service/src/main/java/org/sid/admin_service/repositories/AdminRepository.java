package org.sid.admin_service.repositories;

import org.sid.admin_service.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
}

