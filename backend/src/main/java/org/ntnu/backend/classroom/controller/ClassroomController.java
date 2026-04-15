package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.service.ClassroomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomResponse createClassroom(@Valid @RequestBody CreateClassroomRequest request) {
        return classroomService.createClassroom(request);
    }

    @GetMapping("/{id}")
    public ClassroomResponse getClassroom(@PathVariable int id) {
        return classroomService.getClassroom(id);
    }

    @PatchMapping("/{id}")
    public ClassroomResponse updateClassroom(
            @PathVariable int id,
            @Valid @RequestBody UpdateClassroomRequest request
    ) {
        return classroomService.updateClassroom(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassroom(@PathVariable int id) {
        classroomService.deleteClassroom(id);
    }
}