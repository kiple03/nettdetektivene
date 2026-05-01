package org.ntnu.backend.game.dto.subtype;


import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

/** Answer submission for questions where multiple options can be selected. */
@Getter
@Setter
public class MultiSelectAnswer extends SubmitAnswerRequest {
  /** IDs of the options the student selected. */
  private List<String> selectedIds;
}
