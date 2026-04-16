package org.ntnu.backend.game.dto;

public record MultipleChoiceOptionResponse(
    int id,
    String imageUrl
) implements OptionResponse {}
