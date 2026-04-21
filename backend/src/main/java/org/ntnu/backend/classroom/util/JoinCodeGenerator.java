package org.ntnu.backend.classroom.util;

import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility component responsible for generating secure, readable, and unique 
 * join codes for classrooms.
 */
@Component
@AllArgsConstructor
public class JoinCodeGenerator {

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int LENGTH = 6;
  private static final int MAX_ATTEMPTS = 20;

  private final ClassroomRepository classroomRepository;
  private final SecureRandom random = new SecureRandom();

  /**
   * Generates a unique, 6-character alphanumeric join code.
   * Cross-references the database to ensure the generated code is not currently in use.
   *
   * @return A unique 6-character string.
   * @throws ResponseStatusException if a unique code cannot be generated after the maximum number of attempts.
   */
  public String generateUniqueCode() {
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      StringBuilder sb = new StringBuilder(LENGTH);
      for (int j = 0; j < LENGTH; j++) {
        sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
      }
      String code = sb.toString();
      if (!classroomRepository.existsByJoinCode(code)) {
        return code;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke generere unik kode");
  }
}