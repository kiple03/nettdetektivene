package org.ntnu.backend.game.service;

import java.util.List;
import org.ntnu.backend.game.dto.MultipleChoiceOptionDto;
import org.ntnu.backend.game.dto.QuestionDto;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

  private final QuestionRepository questionRepository;


  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public QuestionResponse getQuestionById(int id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Question not found"));

    LevelType type = question.getLevel().getLevelType();
    List<Option> options = question.getOptions();

    List<?> optionDtos = switch (type) {

      case MULTIPLE_CHOICE -> options.stream()
          .map(o -> toMultipleChoiceDto((MultipleChoiceOption) o))
          .toList();
      case TEXT_QUIZ -> null;
      case PICTURE_QUIZ -> null;
      case MINIGAME -> null;
      case BOSS_BATTLE -> null;
      case TUTORIAL -> null;
    };

    return new QuestionResponse(
        question.getId(),
        question.getQuestionText(),
        (List<Option>) optionDtos
    );
  }


  private MultipleChoiceOptionDto toMultipleChoiceDto(MultipleChoiceOption option) {
    return new MultipleChoiceOptionDto(
        option.getId(),
        option.getImageUrl(),
        option.isCorrect(),
        option.getExplanation()
    );
  }
}
