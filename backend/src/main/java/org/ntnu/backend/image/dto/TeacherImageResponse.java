package org.ntnu.backend.image.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for an image visible to teachers.
 *
 * @param id               unique image identifier
 * @param url              public URL of the image
 * @param originalFilename filename provided at upload time
 * @param uploadedAt       timestamp when the image was uploaded
 */
public record TeacherImageResponse(
        Long id,
        String url,
        String originalFilename,
        LocalDateTime uploadedAt
) {}
