package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse createClassroom(
            @Valid @RequestBody CreateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.createClassroom(request, principal.getUserId());
    }
}
