package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;

@Value
@Builder
public class MysterySubmissionResponse {
    UUID id;
    String studentUsername;
    String content;
    String imageUrl;
    Instant submittedAt;
    MysteryStatusEnum status;
    boolean featured;
}
