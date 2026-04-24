package org.sid.admin_service.repositories;

import org.sid.admin_service.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

