package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.service.ClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    // ── Teacher-only endpoints ────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse createClassroom(@Valid @RequestBody CreateClassroomRequest request) {
        return classroomService.createClassroom(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse updateClassroom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClassroomRequest request
    ) {
        return classroomService.updateClassroom(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteClassroom(@PathVariable UUID id) {
        classroomService.deleteClassroom(id);
    }

    // ── Shared endpoints (teacher + student) ─────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ClassroomResponse getClassroom(@PathVariable UUID id) {
        return classroomService.getClassroom(id);
    }

    // ── Student join endpoint ─────────────────────────────────────────────────

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('STUDENT')")
    public ClassroomResponse joinClassroom(@Valid @RequestBody JoinClassroomRequest request,
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return classroomService.joinClassroom(request, principal.getUserId());
    }
}
