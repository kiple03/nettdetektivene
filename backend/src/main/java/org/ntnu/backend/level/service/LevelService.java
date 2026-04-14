package java.org.ntnu.backend.level.service;

import java.org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import java.org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.springframework.stereotype.Service;

@Service
public class LevelService {
    public ValidateLevelAnswerResponse response(
            int levelID,
            ValidateLevelAnswerRequest request
    ) {
        if (request.getOptionId == null || request.getOptionId().isBlank()) {
            throw new ValidateLevelAnswerResponse(false, "Du må velge ett alternativ!", false);
        }

        if (!hasAccessToLevel(levelID)) {
            return new ValidateLevelAnswerResponse(false, "Du har ikke åpnet dette nivået enda!", false);
        }

        boolean correct = isCorrectAnswer(levelID, request.getOptionId());

        if (!correct) {
            return new ValidateLevelAnswerResponse(false, "Du fikk feil, prøv igjen", false);
        }

        unlockNextLevelForUser(levelID);

        return new ValidateLevelAnswerResponse(false, "Riktig svar!", false)
    }

    private boolean hasAccessToLevel(int levelID) {
        return levelID == 1;
    }

    private boolean isCorrectAnswer(int levelID, String optionId) {
        if (levelID == 1) {
            return optionId.equals("A");
        }

        return false;
    }
}