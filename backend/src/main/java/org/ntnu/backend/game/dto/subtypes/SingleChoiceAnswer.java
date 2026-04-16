package org.ntnu.backend.game.dto.subtypes;

import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

@Getter
public class SingleChoiceAnswer extends SubmitAnswerRequest {
  private String selectedId;
}

