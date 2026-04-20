package org.sid.freelancer_service.repository;

import org.sid.freelancer_service.entity.CvDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CvDocumentRepository extends JpaRepository<CvDocument, Long> {
    Optional<CvDocument> findByProfileId(String profileId);
}
