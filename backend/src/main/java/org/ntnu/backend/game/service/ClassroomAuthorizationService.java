package org.ntnu.backend.game.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Verifies that the authenticated user has access to a given classroom.
 *
 * <p>Authorization rules differ by role:
 * <ul>
 *   <li>{@link RoleEnum#STUDENT}: the classroom ID must match the one in their JWT
 *       (set during enrollment). A student can only access their own classroom.</li>
 *   <li>{@link RoleEnum#STUDENT}: must be registered as a teacher for the classroom
 *       via the {@code classroom_teacher} join table.</li>
 * </ul>
 *
 * <p>Throws 403 Forbidden if access is denied.
 *
 * @see JwtAuthenticatedPrincipal
 */
@Service
@AllArgsConstructor
public class ClassroomAuthorizationService {

  private final ClassroomTeacherRepository classroomTeacherRepository;

  /**
   * Checks that the user has access to the specified classroom.
   *
   * @param principal the authenticated user from the JWT
   * @param classroomId the classroom being accessed
   * @throws AccessDeniedException 403 if the user doesn't have access
   */
  public void verifyAccess(JwtAuthenticatedPrincipal principal, UUID classroomId) {
    if (principal.isStudent()) {
      if (!classroomId.equals(principal.getClassroomId())) {
        throw new AccessDeniedException("You do not have permission to access this classroom");
      }
    } else if (principal.isTeacher()) {
      boolean owns = classroomTeacherRepository.existsByClassroomIdAndUserId(classroomId, principal.getUserId());
      if (!owns) {
        throw new AccessDeniedException("You do not have permission to access this classroom");
      }
    }
  }
}