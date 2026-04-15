package org.ntnu.backend.game.service;

import lombok.AllArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.game.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.OptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;
    private final OptionRepository optionRepository;


    public LevelResponse getLevel(int levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nivå ikke funnet"));

        var questions = level.getQuestions().stream().map(q -> {
            var options = q.getOptions().stream()
                    .map(o -> new LevelResponse.OptionDto(o.getId(), o.getText()))
                    .toList();
            return new LevelResponse.QuestionDto(q.getId(), q.getText(), options);
        }).toList();

        return new LevelResponse(level.getId(), level.getTitle(), level.getDescription(), questions);
    }

    public ValidateLevelAnswerResponse validateAnswer(int levelId, ValidateLevelAnswerRequest request) {
        int optionId = Integer.parseInt(request.getOptionId());

        Option option = optionRepository.findByIdAndQuestion_Level_Id(optionId, levelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kan ikke velge dette alternativet"));

        if (option.isCorrect()) {
            return new ValidateLevelAnswerResponse(true, "Riktig svar!", true);
        }

        return new ValidateLevelAnswerResponse(false, "Feil svar, prøv igjen.", false);
    }
}