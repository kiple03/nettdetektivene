package org.ntnu.backend.level.service;

import org.ntnu.backend.level.dto.LevelResponse;
import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.level.model.Level;
import org.ntnu.backend.level.model.Option;
import org.ntnu.backend.level.repository.LevelRepository;
import org.ntnu.backend.level.repository.OptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LevelService {

    private final LevelRepository levelRepository;
    private final OptionRepository optionRepository;

    public LevelService(LevelRepository levelRepository, OptionRepository optionRepository) {
        this.levelRepository = levelRepository;
        this.optionRepository = optionRepository;
    }

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