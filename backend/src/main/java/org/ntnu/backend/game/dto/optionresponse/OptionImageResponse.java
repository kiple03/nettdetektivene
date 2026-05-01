package org.ntnu.backend.game.dto.optionresponse;

public record OptionImageResponse(
    int optionId,
    String responseImageUrl
) implements OptionResponse {}