package org.ntnu.backend.teacher.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.enrollment.model.Enrollment;
import org.ntnu.backend.enrollment.repository.EnrollmentRepository;
import org.ntnu.backend.level.model.LevelCompletion;
import org.ntnu.backend.level.repository.LevelCompletionRepository;
import org.ntnu.backend.mystery.model.MysterySubmission;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.notepad.repository.NotepadEntryRepository;
import org.ntnu.backend.stop.model.Medal;
import org.ntnu.backend.stop.repository.MedalRepository;
import org.ntnu.backend.stop.repository.StopRepository;
import org.ntnu.backend.teacher.dto.ClassroomSummary;
import org.ntnu.backend.teacher.dto.MysterySubmissionResponse;
import org.ntnu.backend.teacher.dto.NotificationResponse;
import org.ntnu.backend.teacher.dto.PatchMysteryRequest;
import org.ntnu.backend.teacher.dto.StudentDetail;
import org.ntnu.backend.teacher.dto.StudentSummary;
import org.ntnu.backend.teacher.dto.StopSummary;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeacherService {

    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MysterySubmissionRepository mysterySubmissionRepository;
    private final MedalRepository medalRepository;
    private final LevelCompletionRepository levelCompletionRepository;
    private final NotepadEntryRepository notepadEntryRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;

    public TeacherService(
            ClassroomTeacherRepository classroomTeacherRepository,
            ClassroomRepository classroomRepository,
            EnrollmentRepository enrollmentRepository,
            MysterySubmissionRepository mysterySubmissionRepository,
            MedalRepository medalRepository,
            LevelCompletionRepository levelCompletionRepository,
            NotepadEntryRepository notepadEntryRepository,
            StopRepository stopRepository,
            UserRepository userRepository
    ) {
        this.classroomTeacherRepository = classroomTeacherRepository;
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mysterySubmissionRepository = mysterySubmissionRepository;
        this.medalRepository = medalRepository;
        this.levelCompletionRepository = levelCompletionRepository;
        this.notepadEntryRepository = notepadEntryRepository;
        this.stopRepository = stopRepository;
        this.userRepository = userRepository;
    }

    // ── 1. List classrooms ─────────────────────────────────────────────────────

    public List<ClassroomSummary> listClassrooms(UUID teacherId) {
        return classroomTeacherRepository.findAllByIdUserId(teacherId).stream()
                .map(ct -> {
                    Classroom c = ct.getClassroom();
                    long count = enrollmentRepository.countByClassroomIdAndLeftAtIsNull(c.getId());
                    return new ClassroomSummary(
                            c.getId().toString(),
                            c.getTitle(),
                            c.getDescription(),
                            c.getJoinCode(),
                            c.getCreatedAt().toString(),
                            count
                    );
                })
                .toList();
    }

    // ── 2. Create classroom ────────────────────────────────────────────────────

    public ClassroomSummary createClassroom(UUID teacherId, String title, String description) {
        Classroom classroom = new Classroom();
        classroom.setTitle(title);
        classroom.setDescription(description);
        classroom.setJoinCode(generateUniqueCode());
        Classroom saved = classroomRepository.save(classroom);

        var teacherUser = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ClassroomTeacher ct = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(saved.getId(), teacherId))
                .classroom(saved)
                .user(teacherUser)
                .isOwner(true)
                .build();
        classroomTeacherRepository.save(ct);

        return new ClassroomSummary(
                saved.getId().toString(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getJoinCode(),
                saved.getCreatedAt().toString(),
                0
        );
    }

    // ── 3. Get classroom detail ────────────────────────────────────────────────

    public ClassroomSummary getClassroom(UUID teacherId, UUID classroomId) {
        assertTeacherOf(teacherId, classroomId);
        Classroom c = findClassroom(classroomId);
        long count = enrollmentRepository.countByClassroomIdAndLeftAtIsNull(classroomId);
        return new ClassroomSummary(
                c.getId().toString(),
                c.getTitle(),
                c.getDescription(),
                c.getJoinCode(),
                c.getCreatedAt().toString(),
                count
        );
    }

    // ── 4. Update classroom ────────────────────────────────────────────────────

    public ClassroomSummary updateClassroom(UUID teacherId, UUID classroomId, String title, String description) {
        assertTeacherOf(teacherId, classroomId);
        Classroom c = findClassroom(classroomId);
        if (title != null && !title.isBlank()) {
            c.setTitle(title.trim());
        }
        if (description != null) {
            c.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        classroomRepository.save(c);
        long count = enrollmentRepository.countByClassroomIdAndLeftAtIsNull(classroomId);
        return new ClassroomSummary(
                c.getId().toString(),
                c.getTitle(),
                c.getDescription(),
                c.getJoinCode(),
                c.getCreatedAt().toString(),
                count
        );
    }

    // ── 5. List students ───────────────────────────────────────────────────────

    public List<StudentSummary> listStudents(UUID teacherId, UUID classroomId) {
        assertTeacherOf(teacherId, classroomId);
        return enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroomId).stream()
                .map(e -> new StudentSummary(
                        e.getUser().getId().toString(),
                        e.getUser().getUsername(),
                        medalRepository.countByEnrollmentId(e.getId()),
                        levelCompletionRepository.countByEnrollmentId(e.getId())
                ))
                .toList();
    }

    // ── 6. Student detail ──────────────────────────────────────────────────────

    public StudentDetail getStudentDetail(UUID teacherId, UUID classroomId, UUID studentId) {
        assertTeacherOf(teacherId, classroomId);
        Enrollment enrollment = enrollmentRepository
                .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not enrolled"));

        List<Medal> medals = medalRepository.findByEnrollmentId(enrollment.getId());
        List<LevelCompletion> levelCompletions = levelCompletionRepository.findByEnrollmentId(enrollment.getId());
        var notepadEntries = notepadEntryRepository.findByEnrollmentId(enrollment.getId());

        return new StudentDetail(
                enrollment.getUser().getId().toString(),
                enrollment.getUser().getUsername(),
                medals.stream().map(m -> new StudentDetail.MedalEntry(
                        m.getStop().getId().toString(),
                        m.getStop().getTitle(),
                        m.getEarnedAt().toString()
                )).toList(),
                levelCompletions.stream().map(lc -> new StudentDetail.LevelEntry(
                        lc.getLevel().getId().toString(),
                        lc.getCompletedAt().toString()
                )).toList(),
                notepadEntries.stream().map(n -> new StudentDetail.NotepadEntryItem(
                        n.getId().toString(),
                        n.getStop() != null ? n.getStop().getId().toString() : null,
                        n.getType().name(),
                        n.getContent(),
                        n.getCreatedAt().toString()
                )).toList()
        );
    }

    // ── 7. List mystery submissions ────────────────────────────────────────────

    public List<MysterySubmissionResponse> listMysterySubmissions(UUID teacherId, UUID classroomId) {
        assertTeacherOf(teacherId, classroomId);
        return mysterySubmissionRepository.findByEnrollmentClassroomId(classroomId).stream()
                .map(this::toMysteryResponse)
                .toList();
    }

    // ── 8. Patch mystery submission ────────────────────────────────────────────

    public MysterySubmissionResponse patchMysterySubmission(
            UUID teacherId, UUID classroomId, UUID submissionId, PatchMysteryRequest req) {
        assertTeacherOf(teacherId, classroomId);
        MysterySubmission submission = mysterySubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        if (!submission.getEnrollment().getClassroom().getId().equals(classroomId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission not in this classroom");
        }

        if (req.getStatus() != null) {
            MysteryStatusEnum newStatus = switch (req.getStatus()) {
                case "approved" -> MysteryStatusEnum.APPROVED;
                case "rejected" -> MysteryStatusEnum.REJECTED;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
            };
            submission.setStatus(newStatus);
            var teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            submission.setReviewedByTeacher(teacher);
            submission.setReviewedAt(Instant.now());
        }
        if (req.getFeatured() != null) {
            submission.setFeatured(req.getFeatured());
        }

        mysterySubmissionRepository.save(submission);
        return toMysteryResponse(submission);
    }

    // ── 9. Notifications ───────────────────────────────────────────────────────

    public List<NotificationResponse> getNotifications(UUID teacherId) {
        List<NotificationResponse> notifications = new ArrayList<>();

        levelCompletionRepository.findRecentByTeacherId(teacherId).forEach(lc -> {
            String username = lc.getEnrollment().getUser().getUsername();
            String classroomId = lc.getEnrollment().getClassroom().getId().toString();
            notifications.add(new NotificationResponse(
                    "LEVEL_COMPLETION",
                    username + " completed a level",
                    classroomId,
                    username,
                    lc.getCompletedAt().toString()
            ));
        });

        mysterySubmissionRepository.findRecentByTeacherId(teacherId).forEach(ms -> {
            String username = ms.getEnrollment().getUser().getUsername();
            String classroomId = ms.getEnrollment().getClassroom().getId().toString();
            notifications.add(new NotificationResponse(
                    "MYSTERY_SUBMISSION",
                    username + " submitted a mystery",
                    classroomId,
                    username,
                    ms.getSubmittedAt().toString()
            ));
        });

        notifications.sort(Comparator.comparing(NotificationResponse::timestamp).reversed());
        return notifications;
    }

    // ── 10. List stops ─────────────────────────────────────────────────────────

    public List<StopSummary> listStops(UUID teacherId, UUID classroomId) {
        assertTeacherOf(teacherId, classroomId);
        return stopRepository.findByClassroomIdOrderByDisplayOrderAsc(classroomId).stream()
                .map(s -> new StopSummary(
                        s.getId().toString(),
                        s.getTitle(),
                        s.getDescription(),
                        s.getDisplayOrder()
                ))
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void assertTeacherOf(UUID teacherId, UUID classroomId) {
        if (!classroomTeacherRepository.existsByIdClassroomIdAndIdUserId(classroomId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a teacher of this classroom");
        }
    }

    private Classroom findClassroom(UUID id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom not found"));
    }

    private MysterySubmissionResponse toMysteryResponse(MysterySubmission ms) {
        return new MysterySubmissionResponse(
                ms.getId().toString(),
                ms.getEnrollment().getUser().getUsername(),
                ms.getContent(),
                ms.getImageUrl(),
                ms.getStatus().name(),
                ms.isFeatured(),
                ms.getSubmittedAt().toString(),
                ms.getReviewedAt() != null ? ms.getReviewedAt().toString() : null
        );
    }

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 20;
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int j = 0; j < CODE_LENGTH; j++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!classroomRepository.existsByJoinCode(code)) {
                return code;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate unique code");
    }
}
