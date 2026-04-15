package org.ntnu.backend.game.controller;

import org.ntnu.backend.game.dto.QuestionDto;
import org.ntnu.backend.game.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public class QuestionController {

  private final QuestionService questionService;

  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping("/questions/{id")
  public QuestionDto getQuestion(@PathVariable int id) {
    return questionService.getQuestionById(id);
  }
}
