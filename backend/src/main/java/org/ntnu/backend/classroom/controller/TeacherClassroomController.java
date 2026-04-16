package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/classrooms")
@AllArgsConstructor
public class TeacherClassroomController {

    private final TeacherClassroomService teacherClassroomService;

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

    @GetMapping("/{id}/mystery")
    @PreAuthorize("hasRole('TEACHER')")
    public List<MysterySubmissionResponse> listMysterySubmissions(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listMysterySubmissions(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse createClassroom(
            @Valid @RequestBody CreateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.createClassroom(request, principal.getUserId());
    }
}
