package org.ntnu.backend.game.dto.subtype;

import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

/** Answer payload for single-choice questions. */
@Getter
public class SingleChoiceAnswer extends SubmitAnswerRequest {
  /** ID of the selected option. */
  private int selectedId;
}

