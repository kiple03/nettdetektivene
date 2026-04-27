package org.ntnu.backend.classroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.CreateTeacherQuestionRequest;
import org.ntnu.backend.classroom.service.TeacherQuestionService;
import org.ntnu.backend.game.dto.TeacherQuestionResponse;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/classrooms/{classroomId}/levels/{levelId}/questions")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherQuestionController {

    private final TeacherQuestionService teacherQuestionService;

    @GetMapping
    public List<TeacherQuestionResponse> listQuestions(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherQuestionService.listQuestionsForTeacher(classroomId, levelId, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherQuestionResponse createQuestion(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @RequestBody @Valid CreateTeacherQuestionRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherQuestionService.createQuestion(classroomId, levelId, request, principal);
    }
}
