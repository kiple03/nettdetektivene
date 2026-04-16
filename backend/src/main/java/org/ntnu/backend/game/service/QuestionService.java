package org.ntnu.backend.game.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;

import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;

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
        type,
        question.getQuestionText(),
        (List<Option>) optionDtos
    );
  }


  private MultipleChoiceOptionResponse toMultipleChoiceDto(MultipleChoiceOption option) {
    return new MultipleChoiceOptionResponse(
        option.getId(),
        option.getImageUrl(),
        option.getIsCorrect(),
        option.getExplanation()
    );
  }
}
