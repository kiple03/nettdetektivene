package org.ntnu.backend.game.dto;

public record SubmitAnswerResponse(
    boolean correct,
    String feedback        // explanation text, or null if correct
) {}
