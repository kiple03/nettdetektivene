package org.ntnu.backend.game.dto.progress;

import java.util.List;

public record SubmitLevelRequest(
    List<QuestionAnswer> answers
) {}
