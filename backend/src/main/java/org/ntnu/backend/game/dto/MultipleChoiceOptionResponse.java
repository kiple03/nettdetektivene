package org.ntnu.backend.game.dto;

import org.ntnu.backend.game.dto.subtypes.OptionResponse;

public record MultipleChoiceOptionResponse(
    int id,
    String imageUrl
) implements OptionResponse {}
