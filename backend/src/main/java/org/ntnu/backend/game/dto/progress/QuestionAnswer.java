package org.ntnu.backend.game.dto.progress;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;

public record QuestionAnswer(
    int questionId,
    SubmitAnswerRequest answer
) {}