package org.ntnu.backend.game.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.ntnu.backend.exception.InvalidOptionFormatException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;

public class OptionParserTest {

  private final OptionParser parser = new OptionParser();

  @Test
  void parseMultipleChoice_validJson_returnsListOfOptions() {
    String json = """
    [
      { "id": 1, "text": "A", "imageUrl": "", "isCorrect": false, "explanation": "Reason A" },
      { "id": 2, "text": "B", "imageUrl": "", "isCorrect": true, "explanation": "Reason B" }
    ]
    """;


    List<Option> result = parser.parse(json, LevelType.MULTIPLE_CHOICE);

    assertEquals(2, result.size());
    assertInstanceOf(MultipleChoiceOption.class, result.getFirst());
    assertEquals("A", ((MultipleChoiceOption) result.getFirst()).getText());
  }

  @Test
  void parseMultipleChoice_invalidJson_throwsException() {
    String json = """
            [
              { "id": "wrong-type", "text": "A" }
            ]
        """;

    assertThrows(
        InvalidOptionFormatException.class,
        () -> parser.parse(json, LevelType.MULTIPLE_CHOICE)
    );
  }
}
