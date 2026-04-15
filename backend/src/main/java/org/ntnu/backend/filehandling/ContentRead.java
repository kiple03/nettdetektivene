package org.ntnu.backend.filehandling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ntnu.backend.game.model.Option;

public class ContentRead {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static List<Option> read(String json) {
    try {
      return mapper.readValue(json, new TypeReference<List<Option>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Kunne ikke lese options fra JSON", e);
    }
  }
}