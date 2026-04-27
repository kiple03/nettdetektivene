package org.ntnu.backend.game.dto;


import java.util.List;

public record ClassifyItemsOptionResponse(
    String key,
    String text,
    List<String> labels
) implements OptionResponse {}