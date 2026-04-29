package org.ntnu.backend.image.dto;

import java.time.LocalDateTime;

public record TeacherImageResponse(
        Long id,
        String url,
        String originalFilename,
        LocalDateTime uploadedAt
) {}
