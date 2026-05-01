package org.ntnu.backend.game.dto.progress;

import java.util.List;

/**
 * Request payload for submitting all answers in a level.
 *
 * @param answers list of question answers submitted by the student
 */
public record SubmitLevelRequest(
    List<QuestionAnswer> answers
) {}
