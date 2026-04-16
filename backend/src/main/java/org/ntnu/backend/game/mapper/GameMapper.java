package org.ntnu.backend.game.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.springframework.stereotype.Component;

/**
 * Converts game entities into response DTOs.
 */
@Component
@RequiredArgsConstructor
public class GameMapper {


  /**
   * Maps a Stop entity to its overview response, including per-student state.
   *
   * @param stop the stop entity
   * @param locked whether this stop is currently locked for the student
   */
  public StopResponse toStopOverviewResponse(Stop stop, boolean locked) {
    return new StopResponse(
        stop.getId(),
        stop.getName(),
        locked
    );
  }

  /**
   * Maps a Level entity to its response, including per-student lock state.
   */
  public LevelResponse toLevelResponse(Level level, boolean locked) {
    return new LevelResponse(
        level.getId(),
        level.getTitle(),
        level.getDescription(),
        level.getLevelType(),
        locked
    );
  }

  /**
   * Maps a Question entity to its response. The {@code optionContent} is stripped
   * of all answer-carrying fields before being included.
   */
  public QuestionResponse toQuestionResponse(Question question) {
    List<OptionResponse> optionResponses = question.getOptions().stream()
        .map(this::toOptionResponse)
        .toList();

    return new QuestionResponse(
        question.getId(),
        question.getLevel().getLevelType(),
        question.getQuestionText(),
        optionResponses
    );
  }

  private OptionResponse toOptionResponse(Option option) {
    return switch (option) {
      case MultipleChoiceOption mc -> new MultipleChoiceOptionResponse(
          mc.getId(),
          mc.getImageUrl()
      );
    };
  }
}
