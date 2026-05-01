package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a censor word entry associated with a classroom.
 */
@Getter
@Builder
@AllArgsConstructor
public class CensorWordResponse {
    private UUID id;
    private String word;
    /** Timestamp when this censor word was added. */
    private Instant createdAt;
}
