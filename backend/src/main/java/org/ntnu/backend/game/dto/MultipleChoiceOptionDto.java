package org.ntnu.backend.game.dto;

public record MultipleChoiceOptionDto(
    int id,
    String imageUrl,
    boolean isCorrect,
    String explanation
) {}
