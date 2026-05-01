package org.ntnu.backend.game.dto.subtypes;

import java.util.Map;
import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

/** Answer type where each item is mapped individually by item ID to a chosen category/value. */
@Getter
public class PerItemAnswer extends SubmitAnswerRequest {
  /** Maps each item ID to the student's selected answer for that item. */
  private Map<String, String> answers;
}
