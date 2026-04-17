package org.ntnu.backend.game.dto;

public record MultipleChoiceOptionResponse(
    int id,
    String text,
    String imageUrl
) implements OptionResponse {}
