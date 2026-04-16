package org.ntnu.backend.game.service;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.QuestionResponse;

import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final GameMapper gameMapper;

  public QuestionResponse getQuestionById(int id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id));
    return gameMapper.toQuestionResponse(question);
  }
}
