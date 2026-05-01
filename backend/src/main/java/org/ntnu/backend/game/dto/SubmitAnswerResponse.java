package org.ntnu.backend.game.dto;

import java.util.List;

/**
 * Response returned after a player submits an answer to a quiz question.
 *
 * @param correct               whether the submitted answer was correct
 * @param feedback              explanation text shown on wrong answers; {@code null} if correct
 * @param correctOptionIds      IDs of the option(s) that are correct
 * @param wrongSelectedOptionIds IDs of the incorrect options the player selected
 */
public record SubmitAnswerResponse(
    boolean correct,
    String feedback,
    List<String> correctOptionIds,
    List<String> wrongSelectedOptionIds,
    List<OptionImageResponse> responseUrls
) {}
