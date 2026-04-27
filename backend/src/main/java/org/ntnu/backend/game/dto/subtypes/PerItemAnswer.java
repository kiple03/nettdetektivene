package org.ntnu.backend.game.dto.subtypes;

import java.util.Map;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

public class PerItemAnswer extends SubmitAnswerRequest {
  private Map<String, String> answers;
}
