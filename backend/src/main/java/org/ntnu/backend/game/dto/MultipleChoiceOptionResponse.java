package org.ntnu.backend.game.dto;

/**
 * Response DTO representing a single option in a multiple-choice question.
 *
 * @param id       unique identifier of the option
 * @param text     display text of the option
 * @param imageUrl optional URL to an image associated with the option
 */
public record MultipleChoiceOptionResponse(
    int id,
    String text,
    String imageUrl,
    String imageAlt
) implements OptionResponse {}
