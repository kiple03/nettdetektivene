package org.ntnu.backend.game.dto;


import java.util.List;

/**
 * Response DTO for a single option in a classify-items quiz question.
 * Each option presents a draggable item that the player must sort into the correct category.
 *
 * @param key    unique identifier for this option
 * @param text   display text of the item to classify
 * @param labels list of valid category labels this item can be classified under
 */
public record ClassifyItemsOptionResponse(
    String key,
    String text,
    List<String> labels
) implements OptionResponse {}