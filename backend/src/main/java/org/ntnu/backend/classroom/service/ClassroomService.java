package org.ntnu.backend.classroom.service;

import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.classroom.util.JoinCodeGenerator;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;

    private final ClassroomMapper classroomMapper;
    private final JoinCodeGenerator joinCodeGenerator;

    @Transactional
    public ClassroomResponse createClassroom(CreateClassroomRequest request, UUID teacherId) {
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruker ikke funnet"));

        String code = joinCodeGenerator.generateUniqueCode();
        Classroom classroom = classroomMapper.toEntity(request, code);
        Classroom saved = classroomRepository.save(classroom);

        ClassroomTeacher classroomTeacher = ClassroomTeacher.builder()
            .id(new ClassroomTeacherId(saved.getId(), teacherId))
            .classroom(saved)
            .user(teacher)
            .isOwner(true)
            .build();
        classroomTeacherRepository.save(classroomTeacher);

        return classroomMapper.toResponse(saved);
    }


    public ClassroomResponse updateClassroom(UUID id, UpdateClassroomRequest request) {
        Classroom classroom = findOrThrow(id);
        classroomMapper.applyUpdates(classroom, request);
        return classroomMapper.toResponse(classroomRepository.save(classroom));
    }

    public void deleteClassroom(UUID id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet");
        }
        classroomRepository.deleteById(id);
    }

    // ── Student operations ────────────────────────────────────────────────────

    public ClassroomResponse joinClassroom(JoinClassroomRequest request, UUID userId) {
        User student = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Bruker ikke funnet"));

        Classroom classroom = classroomRepository.findByJoinCode(request.getJoinCode())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ugyldig kode"));

        if (enrollmentRepository.existsByUserAndLeftAtIsNull(student)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Allerede i et klasserom");
        }

        Enrollment enrollment = Enrollment.builder()
            .user(student)
            .classroom(classroom)
            .build();
        enrollmentRepository.save(enrollment);

        return classroomMapper.toResponse(classroom);
    }

    public ClassroomResponse getClassroom(UUID id) {
        return classroomMapper.toResponse(findOrThrow(id));
    }

    private Classroom findOrThrow(UUID id) {
        return classroomRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Klasserom ikke funnet"));
    }
}
