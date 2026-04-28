package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.service.ClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for classroom. It's responsible for fetching a classroom and joining one via a code.
 */
@RestController
@RequestMapping("/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    // ── Shared endpoints (teacher + student) ─────────────────────────────────

    /**
     * Controller endpoint GET-request that gets a specific classroom. Any role can use this.
     * @param id the id of the classroom
     * @return a classroom response.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ClassroomResponse getClassroom(@PathVariable UUID id) {
        return classroomService.getClassroom(id);
    }

    // ── Student endpoints ─────────────────────────────────────────────────────

    /**
     * Returns the classroom the authenticated student is currently enrolled in.
     * Returns 204 No Content if the student is not enrolled in any classroom.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClassroomResponse> getMyClassroom(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        Optional<ClassroomResponse> classroom = classroomService.getMyClassroom(principal.getUserId());
        return classroom.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // ── Student join endpoint ─────────────────────────────────────────────────

    /**
     * Controller endpoint POST-request that checkes if a code is valid for a classroom.
     * @param request JSON payload  
     * @param principal authenticated user.
     * @return classroom response 
     */
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('STUDENT')")
    public JoinClassroomResponse joinClassroom(@Valid @RequestBody JoinClassroomRequest request,
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return classroomService.joinClassroom(request, principal.getUserId());
    }
}
