package org.ntnu.backend.game.dto;

public record OptionImageResponse(
    int id,
    String responseImageUrl
) implements OptionResponse {}