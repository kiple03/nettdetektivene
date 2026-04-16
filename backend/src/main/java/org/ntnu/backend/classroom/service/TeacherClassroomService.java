package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TeacherClassroomService {

    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomService classroomService;

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
}
