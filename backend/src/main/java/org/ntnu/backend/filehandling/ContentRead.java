package org.ntnu.backend.filehandling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ntnu.backend.game.model.Option;

/**
 * Utility class for deserializing JSON content into domain model lists.
 *
 * <p>Uses a shared, stateless {@link ObjectMapper} instance for efficiency.
 * This class is not meant to be instantiated.
 */
public class ContentRead {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Deserializes a JSON array string into a list of {@link Option} objects.
   *
   * @param json a JSON array string representing a list of options
   * @return a {@link List} of deserialized {@link Option} instances
   * @throws RuntimeException if the JSON is malformed or cannot be mapped to {@link Option}
   */
  public static List<Option> read(String json) {
    try {
      return mapper.readValue(json, new TypeReference<List<Option>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Kunne ikke lese options fra JSON", e);
    }
  }
}