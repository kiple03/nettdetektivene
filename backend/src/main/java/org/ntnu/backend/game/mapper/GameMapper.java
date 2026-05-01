package org.ntnu.backend.game.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.optionresponse.ClassifyItemsOptionResponse;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.optionresponse.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.optionresponse.PostOfficeOptionResponse;
import org.ntnu.backend.game.dto.optionresponse.PictureQuizOptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.optionresponse.OptionResponse;
import org.ntnu.backend.game.dto.optionresponse.PasswordStrengthOptionResponse;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.PostOfficeQuestion;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;
import org.springframework.stereotype.Component;

/**
 * Converts game entities into response DTOs.
 */
@Component
@RequiredArgsConstructor
public class GameMapper {
  private final OptionParser optionParser;

  /**
   * Maps a Stop entity to its overview response, including per-student lock state.
   *
   * @param stop   the stop entity
   * @param locked whether this stop is locked for the student
   * @return the stop response DTO
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
   *
   * @param level  the level entity
   * @param locked whether this level is locked for the student
   * @return the level response DTO
   */
  public LevelResponse toLevelResponse(Level level, boolean locked) {
    return new LevelResponse(
        level.getId(),
        level.getTitle(),
        level.getDescription(),
        level.getStop().getName(),
        level.getLevelType(),
        level.getOrder(),
        locked
    );
  }

  /**
   * Maps a Question to its student-facing response; answer fields are stripped from options.
   *
   * @param question the question entity
   * @return the question response DTO with sanitised options
   */
  public QuestionResponse toQuestionResponse(Question question) {
    LevelType type = question.getLevel().getLevelType();
    List<Option> options = optionParser.parse(question.getOptionContent(), type);

    List<OptionResponse> optionResponses = options.stream()
            .map(this::toOptionResponse)
            .toList();

    PostOfficeQuestion.Email email = null;
    if (type == LevelType.POST_OFFICE) {
      email = optionParser.parsePostOfficeQuestion(question.getOptionContent()).getEmail();
    }

    return new QuestionResponse(
        question.getId(),
        question.getLevel().getLevelType(),
        question.getQuestionText(),
        optionResponses,
        email
    );
  }

  private OptionResponse toOptionResponse(Option option) {
    return switch (option) {
      case MultipleChoiceOption mc -> new MultipleChoiceOptionResponse(
              mc.getId(),
              mc.getText(),
              mc.getImageUrl(),
          mc.getImageAlt()
      );
      case PostOfficeOption o -> new PostOfficeOptionResponse(o.getId(), o.getType());

      case PictureQuizOption pq -> new PictureQuizOptionResponse(
              pq.id(),
              pq.text(),
              pq.imageUrl(),
          pq.imageAlt()
      );
      case PasswordStrengthOption p -> new PasswordStrengthOptionResponse(
        p.getId(), p.getMinLength(), p.isRequireDigit(), p.isRequireUpper(), p.isRequireLower(), p.isRequireSymbol(),
          p.getImageUrl()
      );
      case ClassifyItemsOption ci -> new ClassifyItemsOptionResponse(
          ci.getKey(),
          ci.getText(),
          ci.getLabels()
      );
    };
  }
}
