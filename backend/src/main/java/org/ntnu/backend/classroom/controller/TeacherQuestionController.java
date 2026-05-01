package org.ntnu.backend.classroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.createquestion.CreateTeacherQuestionRequest;
import org.ntnu.backend.classroom.dto.createquestion.UpdateTeacherQuestionRequest;
import org.ntnu.backend.classroom.service.TeacherQuestionService;
import org.ntnu.backend.config.openapi.ApiErrorResponse;
import org.ntnu.backend.game.dto.teacher.TeacherQuestionResponse;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for teacher management of custom questions within a classroom level.
 * All endpoints require the {@code TEACHER} role and are scoped to a specific classroom and level.
 */
@RestController
@RequestMapping("/teacher/classrooms/{classroomId}/levels/{levelId}/questions")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
@Tag(name = "Teacher Questions", description = "Custom question authoring per classroom level")
public class TeacherQuestionController {

    private final TeacherQuestionService teacherQuestionService;

    /**
     * Returns all questions for a given level in the teacher's classroom.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level identifier
     * @param principal   the authenticated teacher
     * @return list of questions with full teacher-visible details
     */
    @GetMapping
    public List<TeacherQuestionResponse> listQuestions(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherQuestionService.listQuestionsForTeacher(classroomId, levelId, principal);
    }

    /**
     * Creates a new custom question for a level in the teacher's classroom.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level identifier
     * @param request     the question payload (type, text, options, etc.)
     * @param principal   the authenticated teacher
     * @return the newly created question with its assigned ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a custom question for a level")
    @ApiResponse(responseCode = "201", description = "Question created")
    @ApiErrorResponse(responseCode = "400", description = "Malformed question")
    @ApiErrorResponse(responseCode = "404", description = "Level adding to does not exist")
    public TeacherQuestionResponse createQuestion(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @RequestBody @Valid CreateTeacherQuestionRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherQuestionService.createQuestion(classroomId, levelId, request, principal);
    }

    /**
     * Updates an existing question in the teacher's classroom level.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level identifier
     * @param questionId  the question identifier
     * @param request     the updated question payload
     * @param principal   the authenticated teacher
     * @return the updated question
     */
    @PutMapping("/{questionId}")
    @Operation(summary = "Replace an existing question")
    @ApiErrorResponse(responseCode = "400", description = "Malformed question")
    @ApiErrorResponse(responseCode = "404", description = "Question does not exist")
    public TeacherQuestionResponse updateQuestion(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @PathVariable int questionId,
            @RequestBody @Valid UpdateTeacherQuestionRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherQuestionService.updateQuestion(classroomId, levelId, questionId, request, principal);
    }

    /**
     * Deletes a question from a level in the teacher's classroom.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level identifier
     * @param questionId  the question identifier
     * @param principal   the authenticated teacher
     */
    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a created question from classroom")
    @ApiResponse(responseCode = "204", description = "Question deleted")
    @ApiErrorResponse(responseCode = "404", description = "Question does not exist")
    public void deleteQuestion(
            @PathVariable UUID classroomId,
            @PathVariable int levelId,
            @PathVariable int questionId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherQuestionService.deleteQuestion(classroomId, levelId, questionId, principal);
    }
}
