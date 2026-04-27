package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CensorWordResponse {
    private UUID id;
    private String word;
    private Instant createdAt;
}
