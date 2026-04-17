package org.ntnu.backend.game.dto.subtypes;


import java.util.List;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

public class MultiSelectAnswer extends SubmitAnswerRequest {
  private List<String> selectedIds;
}
