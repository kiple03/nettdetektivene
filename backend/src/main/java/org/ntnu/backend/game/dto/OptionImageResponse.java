package org.ntnu.backend.game.dto;

public record OptionImageResponse(
    int optionId,
    String responseImageUrl
) implements OptionResponse {}