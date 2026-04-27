package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.AddCensorWordRequest;
import org.ntnu.backend.classroom.dto.CensorWordResponse;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CoTeacherResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.InviteCoTeacherRequest;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.ReviewMysteryRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateStudentUsernameRequest;
import org.ntnu.backend.classroom.service.CensorWordService;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/classrooms")
@AllArgsConstructor
public class TeacherClassroomController {

    private final TeacherClassroomService teacherClassroomService;
    private final CensorWordService censorWordService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<ClassroomSummaryResponse> listClassrooms(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listClassrooms(principal.getUserId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse getClassroom(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getClassroom(id, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteClassroom(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.deleteClassroom(id, principal);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse updateClassroom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.updateClassroom(id, request, principal);
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentProgressResponse> listStudents(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listStudents(id, principal);
    }

    @GetMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public StudentDetailResponse getStudentDetail(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getStudentDetail(id, studentId, principal);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void removeStudent(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.removeStudent(id, studentId, principal);
    }

    @GetMapping("/{id}/students/{studentId}/medalions")
    @PreAuthorize("hasRole('TEACHER')")
    public List<MedalionResponse> getStudentMedalions(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getStudentMedalions(id, studentId, principal);
    }

    @PatchMapping("/{id}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void updateStudentUsername(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @Valid @RequestBody UpdateStudentUsernameRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.updateStudentUsername(id, studentId, request, principal);
    }

    @GetMapping("/{id}/mystery")
    @PreAuthorize("hasRole('TEACHER')")
    public List<MysterySubmissionResponse> listMysterySubmissions(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listMysterySubmissions(id, principal);
    }

    @PatchMapping("/{id}/mystery/{submissionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public MysterySubmissionResponse reviewMysterySubmission(
            @PathVariable UUID id,
            @PathVariable UUID submissionId,
            @RequestBody ReviewMysteryRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.reviewMysterySubmission(id, submissionId, request, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse createClassroom(
            @Valid @RequestBody CreateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.createClassroom(request, principal.getUserId());
    }

    // TODO: rework implementation
//    @GetMapping("/notifications")
//    @PreAuthorize("hasRole('TEACHER')")
//    public List<NotificationResponse> getNotifications(
//        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
//        return teacherClassroomService.getNotifications(principal.getUserId());
//    }
    @GetMapping("/{id}/teachers")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CoTeacherResponse> listCoTeachers(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listCoTeachers(id, principal);
    }

    @PostMapping("/{id}/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public CoTeacherResponse inviteCoTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody InviteCoTeacherRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.inviteCoTeacher(id, request, principal);
    }

    @DeleteMapping("/{id}/teachers/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void removeCoTeacher(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.removeCoTeacher(id, userId, principal);
    }
}
