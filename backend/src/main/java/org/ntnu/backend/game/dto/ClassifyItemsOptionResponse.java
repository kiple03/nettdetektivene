package org.ntnu.backend.game.dto;


public record ClassifyItemsOptionResponse(
    String key,
    String text
) implements OptionResponse {}