package org.ntnu.backend.filehandling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ntnu.backend.game.model.Option;

/**
 * Utility class for serializing domain model lists into JSON content.
 *
 * <p>Uses a shared, stateless {@link ObjectMapper} instance for efficiency.
 * This class is not meant to be instantiated.
 *
 * @see ContentRead
 */
public class ContentWrite {
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Serializes a list of {@link Option} objects into a JSON array string.
   *
   * @param options the list of options to serialize; must not be {@code null}
   * @return a JSON array string representing the given options
   * @throws RuntimeException if serialization fails due to a non-serializable value
   */
  public static String write(List<Option> options) {
    try {
      return mapper.writeValueAsString(options);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Could not convert options to JSON", e);
    }
  }
}