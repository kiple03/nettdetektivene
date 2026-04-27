package org.ntnu.backend.game.dto.subtypes;


import java.util.List;
import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

@Getter
public class MultiSelectAnswer extends SubmitAnswerRequest {
  private List<String> selectedIds;
}
