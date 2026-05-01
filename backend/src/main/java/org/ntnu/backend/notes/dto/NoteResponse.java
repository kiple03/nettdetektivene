package org.ntnu.backend.notes.dto;

import java.time.Instant;

/**
 * Response payload for a single note.
 *
 * @param id        unique note identifier
 * @param category  note category (e.g. "CLUE", "PERSONAL")
 * @param content   text written by the student
 * @param createdAt when the note was first saved
 * @param updatedAt when the note was last modified
 */
public record NoteResponse(
        String id,
        String category,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}