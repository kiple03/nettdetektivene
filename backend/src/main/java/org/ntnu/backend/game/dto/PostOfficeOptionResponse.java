package org.ntnu.backend.game.dto;

/**
 * Option response for post-office (drag-and-drop sorting) game questions,
 * representing a single item the player must sort into the correct category.
 *
 * @param id   unique identifier of the item
 * @param type the correct category this item belongs to
 */
public record PostOfficeOptionResponse(
    String id,
    String type
) implements OptionResponse {}