package org.ntnu.backend.classroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.service.TeacherStopService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing stop-management endpoints for teachers.
 *
 * <p>All endpoints require the {@code TEACHER} role.
 */
@RestController
@RequestMapping("/teacher/stops")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
@Tag(name="Teacher stops", description = "Custom endpoint for stops the teacher uses")
public class TeacherStopController {

    private final TeacherStopService teacherStopService;

    /**
     * Returns all stops available in the system.
     *
     * @return list of {@link StopResponse} representing every stop
     */
    @GetMapping
    @Operation(summary = "List all available stops in the game")
    public List<StopResponse> listStops() {
        return teacherStopService.listStops();
    }
}
