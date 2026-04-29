package org.ntnu.backend.game.dto.subtypes;

import java.util.Map;
import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

@Getter
public class PerItemAnswer extends SubmitAnswerRequest {
  private Map<String, String> answers;
}
