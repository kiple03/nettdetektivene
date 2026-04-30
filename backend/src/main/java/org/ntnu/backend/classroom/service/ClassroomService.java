package org.ntnu.backend.classroom.service;

import java.util.Optional;
import java.util.UUID;

import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.JoinClassroomRequest;
import org.ntnu.backend.classroom.dto.JoinClassroomResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.classroom.util.JoinCodeGenerator;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.security.UserPrincipal;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;

/**
 * Service class for classroom logic. 
 * Handles the creation, modification, deletion, and student enrollment processes for classrooms.
 */
@Service
@AllArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomCensorWordRepository classroomCensorWordRepository;

    private final ClassroomMapper classroomMapper;
    private final JoinCodeGenerator joinCodeGenerator;
    private final MedalionService medalionService;
    private final JwtService jwtService;

    /**
     * Creates a new classroom, generates a unique join code, and assigns the specified teacher as the owner.
     * @param request the creation request
     * @param teacherId teacher identification
     * @return a classroom response.
     */
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


     /**
     * Updates the details (title or description) of an existing classroom.
     *
     * @param id unique identifier of the classroom to update.
     * @param request The data transfer object containing the updated fields.
     * @return A response object containing the updated classroom details.
     */
    public ClassroomResponse updateClassroom(UUID id, UpdateClassroomRequest request) {
        Classroom classroom = findOrThrow(id);
        classroomMapper.applyUpdates(classroom, request);
        return classroomMapper.toResponse(classroomRepository.save(classroom));
    }

    /**
     * Completely deletes a classroom and cascades the deletion to all associated records,
     * including level completions, enrollments, and teacher links.
     *
     * @param id The unique identifier of the classroom to delete.
     * @throws ResponseStatusException if the classroom does not exist.
     */
    @Transactional
    public void deleteClassroom(UUID id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet");
        }
        enrollmentRepository.deleteByClassroomId(id);
        classroomTeacherRepository.deleteByClassroomId(id);
        classroomCensorWordRepository.deleteByClassroomId(id);
        classroomRepository.deleteById(id);
    }

    // ── Student operations ────────────────────────────────────────────────────

    /**
     * Enrolls a student into a classroom using a valid join code.
     * Verifies that the student exists, the code is valid, and the student is not already in a classroom.
     *
     * @param request The request object containing the join code.
     * @param userId The unique identifier of the student attempting to join.
     * @return A response object containing the details of the joined classroom.
     * @throws ResponseStatusException if the user or code is invalid, or if the user is already enrolled.
     */
    public JoinClassroomResponse joinClassroom(JoinClassroomRequest request, UUID userId) {
        User student = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Bruker ikke funnet"));

        if (student.getUsername() == null || student.getUsername().isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Du må velge et brukernavn før du kan bli med i et klasserom");
        }

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

        medalionService.awardInternalUsage(userId, "Hysj-hysj", "Hemmelig innpass, du kom deg inn i klasserommet");

        String accessToken = jwtService.generateToken(new UserPrincipal(student), classroom.getId());
        ClassroomResponse classroomResponse = classroomMapper.toResponse(classroom);
        return JoinClassroomResponse.builder()
            .id(classroomResponse.getId())
            .title(classroomResponse.getTitle())
            .description(classroomResponse.getDescription())
            .joinCode(classroomResponse.getJoinCode())
            .createdAt(classroomResponse.getCreatedAt())
            .accessToken(accessToken)
            .build();
    }

    /**
     * Retrieves the basic details of a specific classroom.
     *
     * @param id The unique identifier of the classroom to retrieve.
     * @return A response object containing the classroom details.
     */
    public ClassroomResponse getClassroom(UUID id) {
        return classroomMapper.toResponse(findOrThrow(id));
    }

    /**
     * Returns the classroom the student is currently enrolled in, or empty if not enrolled.
     *
     * @param userId The student's user ID.
     * @return An Optional containing the classroom response, or empty if not enrolled.
     */
    @Transactional(readOnly = true)
    public Optional<ClassroomResponse> getMyClassroom(UUID userId) {
        return enrollmentRepository.findByUserIdAndLeftAtIsNull(userId)
            .map(e -> classroomMapper.toResponse(e.getClassroom()));
    }

    /**
     * Removes the student from their current classroom by setting leftAt on the enrollment.
     * Returns a new JWT without classroomId.
     *
     * @param userId The student's user ID.
     * @return A new access token without classroomId.
     */
    @Transactional
    public String leaveClassroom(UUID userId) {
        User student = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruker ikke funnet"));

        Enrollment enrollment = enrollmentRepository.findByUserIdAndLeftAtIsNull(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Ikke i et klasserom"));

        enrollment.setLeftAt(Instant.now());
        enrollmentRepository.save(enrollment);

        return jwtService.generateToken(new UserPrincipal(student), null);
    }

    /**
     * Helper method to find a classroom by its ID or throw an HTTP 404 NOT FOUND exception.
     *
     * @param id The unique identifier of the classroom.
     * @return The retrieved Classroom entity.
     * @throws ResponseStatusException if the classroom cannot be found.
     */
    private Classroom findOrThrow(UUID id) {
        return classroomRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Klasserom ikke funnet"));
    }
}
