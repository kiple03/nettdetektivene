package org.ntnu.backend.classroom.mapper;

import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.stereotype.Component;

/**
 * Mapper Component. It translates data between database models and DTO's.
 */
@Component
public class ClassroomMapper {

  /**
   * Mapper method that takes raw ClassRoom object and converts them to 
   * ClassroomResponse JSON objects for frontend.
   * @param c classroom
   * @return friendly classroom object for frontend
   */
  public ClassroomResponse toResponse(Classroom c) {
    return ClassroomResponse.builder()
        .id(c.getId().toString())
        .title(c.getTitle())
        .description(c.getDescription())
        .joinCode(c.getJoinCode())
        .build();
  }

  /**
   * Mapper method that converts JSON payload into Classroom for database 
   * @param request JSON payload
   * @param joinCode code 
   * @return friendly classroom object for database
   */
  public Classroom toEntity(CreateClassroomRequest request, String joinCode) {
    return Classroom.builder()
        .title(request.getTitle().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .joinCode(joinCode)
        .build();
  }

  /**
   * Mapper method that updates database using classroom objects in database
   * @param classroom instance
   * @param request request
   */
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
