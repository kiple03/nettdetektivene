package org.ntnu.backend.game.service;

import java.util.UUID;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassroomAuthorizationService {

  private final ClassroomRepository classroomRepository;

  public void verifyAccess(JwtAuthenticatedPrincipal principal, UUID classroomId) {
    if (principal.isStudent()) {
      if (!classroomId.equals(principal.getClassroomId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    } else if (principal.isTeacher()) {
      boolean owns = classroomRepository.existsByIdAndUsersId(classroomId, principal.getUserId());
      if (!owns) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    }
  }
}