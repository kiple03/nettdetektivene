package org.ntnu.backend.game.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Removes answer-carrying fields from question content JSON before it is
 * serialized into an API response.
 *
 * <p>Question {@code optionContent} stores both render data and answer data
 * in a single JSON blob. This utility walks the JSON tree and strips any
 * known answer keys so correct answers and explanations never leak to
 * students via GET responses.
 *
 * <p>When adding a new question content shape, any field that carries
 * correct-answer information must use one of the documented key names
 * below,  otherwise it will silently leak to the client.
 *
 * @see org.ntnu.backend.game.mapper.GameMapper
 */
@Component
@RequiredArgsConstructor
public class QuestionContentStripper {

  /** Keys expected at any object level that identify correct answers or hints.
   * Add field matching json structure here that should not lead to client.*/
  private static final Set<String> ANSWER_KEYS = Set.of(
      "correctIds"
  );

  private final ObjectMapper objectMapper;

  /**
   * Returns a version of the given JSON with all known answer keys removed.
   *
   * @param rawJson the raw optionContent JSON string from the database
   * @return the stripped JSON string, safe to include in an API response
   * @throws IllegalStateException if the input is not valid JSON
   */
  public String strip(String rawJson) {
    try {
      JsonNode root = objectMapper.readTree(rawJson);
      stripNode(root);
      return objectMapper.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Invalid question content JSON", e);
    }
  }

  /**
   * Recursively removes answer keys from every object in the tree.
   */
  private void stripNode(JsonNode node) {
    if (node instanceof ObjectNode obj) {
      ANSWER_KEYS.forEach(obj::remove);
      obj.fields().forEachRemaining(entry -> stripNode(entry.getValue()));
    } else if (node.isArray()) {
      node.forEach(this::stripNode);
    }
  }
}