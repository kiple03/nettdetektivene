package org.ntnu.backend.game.mapper;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopOverviewResponse;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.util.QuestionContentStripper;
import org.springframework.stereotype.Component;

/**
 * Converts game entities into response DTOs.
 *
 * <p>For {@link Question}, the raw {@code optionContent} JSON is passed through
 * {@link QuestionContentStripper} so correct answers and explanations are never
 * leaked to the client.
 */
@Component
@RequiredArgsConstructor
public class GameMapper {

  private final QuestionContentStripper stripper;

  /**
   * Maps a Stop entity to its overview response, including per-student state.
   *
   * @param stop the stop entity
   * @param locked whether this stop is currently locked for the student
   */
  public StopOverviewResponse toStopOverviewResponse(Stop stop, boolean locked) {
    return new StopOverviewResponse(
        stop.getId(),
        stop.getName(),
        locked,
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
    return new QuestionResponse(
        question.getId(),
        question.getLevel().getLevelType(),
        question.getQuestionText(),
        stripper.strip(question.getOptionContent())
    );
  }
}
