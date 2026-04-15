package org.ntnu.backend.teacher.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.teacher.dto.ClassroomSummary;
import org.ntnu.backend.teacher.dto.CreateTeacherClassroomRequest;
import org.ntnu.backend.teacher.dto.MysterySubmissionResponse;
import org.ntnu.backend.teacher.dto.NotificationResponse;
import org.ntnu.backend.teacher.dto.PatchMysteryRequest;
import org.ntnu.backend.teacher.dto.StudentDetail;
import org.ntnu.backend.teacher.dto.StudentSummary;
import org.ntnu.backend.teacher.dto.StopSummary;
import org.ntnu.backend.teacher.dto.UpdateTeacherClassroomRequest;
import org.ntnu.backend.teacher.service.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // ── 1. List classrooms ─────────────────────────────────────────────────────

    @GetMapping("/classrooms")
    public List<ClassroomSummary> listClassrooms(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherService.listClassrooms(principal.getUserId());
    }

    // ── 2. Create classroom ────────────────────────────────────────────────────

    @PostMapping("/classrooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomSummary createClassroom(
            @Valid @RequestBody CreateTeacherClassroomRequest req,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.createClassroom(principal.getUserId(), req.getTitle(), req.getDescription());
    }

    // ── 3. Get classroom detail ────────────────────────────────────────────────

    @GetMapping("/classrooms/{id}")
    public ClassroomSummary getClassroom(
            @PathVariable String id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.getClassroom(principal.getUserId(), UUID.fromString(id));
    }

    // ── 4. Update classroom ────────────────────────────────────────────────────

    @PatchMapping("/classrooms/{id}")
    public ClassroomSummary updateClassroom(
            @PathVariable String id,
            @Valid @RequestBody UpdateTeacherClassroomRequest req,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.updateClassroom(principal.getUserId(), UUID.fromString(id), req.getTitle(), req.getDescription());
    }

    // ── 5. List students ───────────────────────────────────────────────────────

    @GetMapping("/classrooms/{id}/students")
    public List<StudentSummary> listStudents(
            @PathVariable String id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.listStudents(principal.getUserId(), UUID.fromString(id));
    }

    // ── 6. Student detail ──────────────────────────────────────────────────────

    @GetMapping("/classrooms/{id}/students/{studentId}")
    public StudentDetail getStudentDetail(
            @PathVariable String id,
            @PathVariable String studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.getStudentDetail(principal.getUserId(), UUID.fromString(id), UUID.fromString(studentId));
    }

    // ── 7. List mystery submissions ────────────────────────────────────────────

    @GetMapping("/classrooms/{id}/mystery")
    public List<MysterySubmissionResponse> listMystery(
            @PathVariable String id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.listMysterySubmissions(principal.getUserId(), UUID.fromString(id));
    }

    // ── 8. Patch mystery submission ────────────────────────────────────────────

    @PatchMapping("/classrooms/{id}/mystery/{submissionId}")
    public MysterySubmissionResponse patchMystery(
            @PathVariable String id,
            @PathVariable String submissionId,
            @RequestBody PatchMysteryRequest req,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.patchMysterySubmission(
                principal.getUserId(), UUID.fromString(id), UUID.fromString(submissionId), req);
    }

    // ── 9. Notifications ───────────────────────────────────────────────────────

    @GetMapping("/notifications")
    public List<NotificationResponse> getNotifications(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherService.getNotifications(principal.getUserId());
    }

    // ── 10. List stops ─────────────────────────────────────────────────────────

    @GetMapping("/classrooms/{id}/stops")
    public List<StopSummary> listStops(
            @PathVariable String id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return teacherService.listStops(principal.getUserId(), UUID.fromString(id));
    }
}
