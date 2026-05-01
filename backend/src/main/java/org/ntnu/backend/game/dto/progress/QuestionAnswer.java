package org.ntnu.backend.game.dto.progress;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;

/**
 * Pairs a question with the player's submitted answer during game progress.
 *
 * @param questionId unique identifier of the question being answered
 * @param answer     the player's submitted answer payload
 */
public record QuestionAnswer(
    int questionId,
    SubmitAnswerRequest answer
) {}