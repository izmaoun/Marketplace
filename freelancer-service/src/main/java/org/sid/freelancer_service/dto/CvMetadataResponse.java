package org.sid.freelancer_service.dto;

import java.time.LocalDateTime;

public record CvMetadataResponse(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        LocalDateTime uploadedAt
) {
}
