package org.ntnu.backend.game.service;

import java.util.List;
import org.ntnu.backend.game.dto.OptionDto;
import org.ntnu.backend.game.dto.QuestionDto;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

  private final QuestionRepository questionRepository;


  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public QuestionDto getQuestionById(int id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Question not found"));

    List<OptionDto> optionDtos = question.getOptions().stream()
        .map(this::toOptionDto)
        .toList();

    return new QuestionDto(
        question.getId(),
        question.getQuestionText(),
        optionDtos
    );
  }

  private OptionDto toOptionDto(Option option) {
    return new OptionDto(
        option.getId(),
        option.getText(),
        option.getImageUrl(),
        option.isCorrect(),
        option.getExplanation()
    );
  }
}
