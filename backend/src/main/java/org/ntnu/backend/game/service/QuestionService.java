package org.ntnu.backend.game.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.QuestionResponse;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.validation.SubmittedAnswerValidator;
import org.ntnu.backend.game.validation.ValidationResult;
import org.springframework.stereotype.Service;
import org.ntnu.backend.game.validation.SubmittedAnswerValidatorRegistry;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final GameMapper gameMapper;
  private final OptionParser optionParser;
  private final SubmittedAnswerValidatorRegistry validatorRegistry;

  public QuestionResponse getQuestionById(int id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id));
    return gameMapper.toQuestionResponse(question);
  }

  public SubmitAnswerResponse submitAnswer(int questionId, SubmitAnswerRequest request) {
    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new QuestionNotFoundException(questionId));

    LevelType type = question.getLevel().getLevelType();
    List<Option> options = optionParser.parse(question.getOptionContent(), type);

    SubmittedAnswerValidator validator = validatorRegistry.forType(type);
    ValidationResult result = validator.validate(options, request);

    return new SubmitAnswerResponse(result.correct(), result.feedback());
  }
}
