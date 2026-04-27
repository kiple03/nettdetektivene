package org.ntnu.backend.classroom.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.service.TeacherQuestionService;
import org.ntnu.backend.game.dto.TeacherQuestionResponse;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
