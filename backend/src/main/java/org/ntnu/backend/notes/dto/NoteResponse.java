package org.ntnu.backend.notes.dto;

import java.time.Instant;

public record NoteResponse(
        String id,
        String category,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}