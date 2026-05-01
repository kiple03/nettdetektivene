package org.ntnu.backend.classroom.controller;

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
public class TeacherStopController {

    private final TeacherStopService teacherStopService;

    /**
     * Returns all stops available in the system.
     *
     * @return list of {@link StopResponse} representing every stop
     */
    @GetMapping
    public List<StopResponse> listStops() {
        return teacherStopService.listStops();
    }
}
