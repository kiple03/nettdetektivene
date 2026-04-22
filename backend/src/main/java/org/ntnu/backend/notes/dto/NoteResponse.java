package org.ntnu.backend.notes.dto;

import java.time.Instant;

/**
 * Request body for creating or updating a personal note.
 *
 * @param content the note content written by the user
 */
public record NoteResponse(
        String id,
        String category,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}