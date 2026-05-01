package org.ntnu.backend.classroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.JoinClassroomRequest;
import org.ntnu.backend.classroom.dto.JoinClassroomResponse;
import org.ntnu.backend.classroom.service.ClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for student-facing classroom operations: retrieving classroom details,
 * checking current enrollment, and joining a classroom via a join code.
 *
 * <p>Base path: {@code /classrooms}</p>
 */
@RestController
@RequestMapping("/classrooms")
@Tag(name = "Student Classroom", description = "Join, leave and view classrooms as a student")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    /**
     * Retrieves a classroom by its UUID. Accessible to both teachers and students.
     *
     * @param id the UUID of the classroom to retrieve
     * @return the classroom details
     * @throws org.ntnu.backend.exception.ApiException with 404 if no classroom with that ID exists
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get classroom by ID")
    @ApiResponse(responseCode = "404", description = "Classroom not found")
    public ClassroomResponse getClassroom(@PathVariable UUID id) {
        return classroomService.getClassroom(id);
    }

    /**
     * Returns the classroom the authenticated student is currently enrolled in.
     * Returns 204 No Content if the student is not enrolled in any classroom.
     *
     * @param principal the authenticated student principal
     * @return 200 with the classroom, or 204 if not enrolled
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Get current enrollment",
        description = "Returns the classroom the student is enrolled in"
    )
    @ApiResponse(responseCode = "200", description = "Enrolled classroom returned")
    @ApiResponse(responseCode = "204", description = "Student is not enrolled in any classroom")
    public ResponseEntity<ClassroomResponse> getMyClassroom(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        Optional<ClassroomResponse> classroom = classroomService.getMyClassroom(principal.getUserId());
        return classroom.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Enrolls the authenticated student in a classroom identified by the provided join code.
     *
     * @param request   JSON body containing the join code
     * @param principal the authenticated student principal
     * @return a response containing the classroom info the student has joined
     * @throws org.ntnu.backend.exception.ApiException with 404 if the join code is invalid,
     *         or 409 if the student is already enrolled
     */
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Join a classroom via join code")
    @ApiResponse(responseCode = "200", description = "Successfully joined classroom")
    @ApiResponse(responseCode = "404", description = "Invalid join code")
    @ApiResponse(responseCode = "409", description = "Student is already enrolled in a classroom")
    public JoinClassroomResponse joinClassroom(@Valid @RequestBody JoinClassroomRequest request,
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return classroomService.joinClassroom(request, principal.getUserId());
    }

    /**
     * Removes the authenticated student from their current classroom.
     * Returns a new access token without classroomId.
     */
    @DeleteMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Leave current classroom",
        description = "Removes the student from their classroom and returns a new access token with classroomId claim removed"
    )
    @ApiResponse(responseCode = "200", description = "Left classroom, new accessToken in body")
    public Map<String, String> leaveClassroom(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        String accessToken = classroomService.leaveClassroom(principal.getUserId());
        return Map.of("accessToken", accessToken);
    }
}
