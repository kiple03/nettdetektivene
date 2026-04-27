package org.ntnu.backend.game.dto.subtypes;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

@Getter
@Setter
public class MultiSelectAnswer extends SubmitAnswerRequest {
  private List<String> selectedIds;
}
