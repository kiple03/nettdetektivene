package org.ntnu.backend.classroom.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/stops")
@AllArgsConstructor
public class TeacherStopsController {

    private final TeacherClassroomService teacherClassroomService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<StopResponse> listStops() {
        return teacherClassroomService.listStops();
    }
}
