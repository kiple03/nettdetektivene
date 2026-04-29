package org.ntnu.backend.game.dto;

import java.util.List;

public record SubmitAnswerResponse(
    boolean correct,
    String feedback,        // explanation text, or null if correct
    List<String> correctOptionIds,
    List<String> wrongSelectedOptionIds
) {}
