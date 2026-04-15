package org.ntnu.backend.classroom.mapper;

import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.stereotype.Component;

@Component
public class ClassroomMapper {

  public ClassroomResponse toResponse(Classroom c) {
    return ClassroomResponse.builder()
        .id(c.getId().toString())
        .title(c.getTitle())
        .description(c.getDescription())
        .joinCode(c.getJoinCode())
        .build();
  }

  public Classroom toEntity(CreateClassroomRequest request, String joinCode) {
    return Classroom.builder()
        .title(request.getTitle().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .joinCode(joinCode)
        .build();
  }

  public void applyUpdates(Classroom classroom, UpdateClassroomRequest request) {
    if (request.getTitle() != null && !request.getTitle().isBlank()) {
      classroom.setTitle(request.getTitle().trim());
    }
    if (request.getDescription() != null) {
      classroom.setDescription(
          request.getDescription().isBlank() ? null : request.getDescription().trim());
    }
  }
}
