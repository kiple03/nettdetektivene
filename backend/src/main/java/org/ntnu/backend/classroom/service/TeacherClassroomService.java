package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class TeacherClassroomService {

    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomService classroomService;
    private final ClassroomAuthorizationService classroomAuthorizationService;

    public ClassroomResponse createClassroom(CreateClassroomRequest request, UUID teacherId) {
        return classroomService.createClassroom(request, teacherId);
    }

    public List<ClassroomSummaryResponse> listClassrooms(UUID teacherId) {
        return classroomTeacherRepository
            .findClassroomsWithStudentCountByTeacherId(teacherId)
            .stream()
            .map(row -> ClassroomSummaryResponse.builder()
                .id(row[0].toString())
                .title((String) row[1])
                .description((String) row[2])
                .joinCode((String) row[3])
                .createdAt((LocalDateTime) row[4])
                .studentCount(((Number) row[5]).longValue())
                .build())
            .toList();
    }

    public ClassroomResponse updateClassroom(UUID classroomId, UpdateClassroomRequest request, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return classroomService.updateClassroom(classroomId, request);
    }

    public ClassroomResponse getClassroom(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ClassroomResponse.builder()
            .id(classroom.getId().toString())
            .title(classroom.getTitle())
            .description(classroom.getDescription())
            .joinCode(classroom.getJoinCode())
            .createdAt(classroom.getCreatedAt())
            .build();
    }
}
