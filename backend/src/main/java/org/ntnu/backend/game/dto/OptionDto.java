package org.ntnu.backend.game.dto;

public record OptionDto (
    int id,
    String text,
    String imageUrl,
    boolean isCorrect,
    String explanation
) {}
