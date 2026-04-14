package org.ntnu.backend.level.service;

import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.springframework.stereotype.Service;

@Service
public class LevelService {

    public ValidateLevelAnswerResponse validateAnswer(
            int levelId,
            ValidateLevelAnswerRequest request
    ) {
        boolean correct = isCorrectAnswer(levelId, request.getOptionId());

        if (correct) {
            return new ValidateLevelAnswerResponse(true, "Correct answer!", true);
        }

        return new ValidateLevelAnswerResponse(false, "Wrong answer, try again.", false);
    }

    private boolean isCorrectAnswer(int levelId, String optionId) {
        if (levelId == 1) {
            return "A".equals(optionId);
        }

        return false;
    }
}