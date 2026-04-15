package org.ntnu.backend.filehandling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ntnu.backend.game.model.Option;

public class ContentWrite {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String write(List<Option> options) {
    try {
      return mapper.writeValueAsString(options);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not convert options to JSON", e);
    }
  }
}