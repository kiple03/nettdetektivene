package org.ntnu.backend.classroom.controller;

import java.util.List;
import java.util.UUID;

import org.ntnu.backend.classroom.dto.censor.AddCensorWordRequest;
import org.ntnu.backend.classroom.dto.censor.CensorWordResponse;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CoTeacherResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.InviteCoTeacherRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateStudentUsernameRequest;
import org.ntnu.backend.classroom.service.CensorWordService;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * REST controller exposing classroom-management endpoints for teachers.
 * All endpoints require the {@code TEACHER} role.
 * Base path: {@code /teacher/classrooms}.
 */
@RestController
@RequestMapping("/teacher/classrooms")
@AllArgsConstructor
public class TeacherClassroomController {

    private final TeacherClassroomService teacherClassroomService;
    private final CensorWordService censorWordService;

    /**
     * Returns a summary list of all classrooms owned or co-taught by the authenticated teacher.
     *
     * @param principal the authenticated teacher
     * @return list of classroom summaries
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<ClassroomSummaryResponse> listClassrooms(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listClassrooms(principal.getUserId());
    }

    /**
     * Returns full details of a specific classroom.
     *
     * @param id        classroom UUID
     * @param principal the authenticated teacher; must have access to this classroom
     * @return classroom details
     * @throws org.springframework.web.server.ResponseStatusException 403 if the teacher does not have access
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse getClassroom(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getClassroom(id, principal);
    }

    /**
     * Permanently deletes a classroom and all associated data.
     *
     * @param id        classroom UUID
     * @param principal the authenticated teacher; must be the owner
     * @throws org.springframework.web.server.ResponseStatusException 403 if the teacher is not the owner
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteClassroom(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.deleteClassroom(id, principal);
    }

    /**
     * Applies a partial update to classroom metadata (e.g. name, description).
     *
     * @param id        classroom UUID
     * @param request   fields to update
     * @param principal the authenticated teacher; must have access to this classroom
     * @return updated classroom details
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse updateClassroom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.updateClassroom(id, request, principal);
    }

    /**
     * Lists all enrolled students together with their progress in the given classroom.
     *
     * @param id        classroom UUID
     * @param principal the authenticated teacher
     * @return list of student progress records
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentProgressResponse> listStudents(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listStudents(id, principal);
    }

    /**
     * Returns detailed information about a single student within a classroom,
     * including level-by-level progress.
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param principal the authenticated teacher
     * @return student detail with progress breakdown
     */
    @GetMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public StudentDetailResponse getStudentDetail(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getStudentDetail(id, studentId, principal);
    }

    /**
     * Removes a student from the classroom.
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param principal the authenticated teacher; must have access to this classroom
     */
    @DeleteMapping("/{id}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void removeStudent(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.removeStudent(id, studentId, principal);
    }

    /**
     * Returns the detective notebook entries written by a student across all levels.
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param principal the authenticated teacher
     * @return notebook overview grouped by level
     */
    @GetMapping("/{id}/students/{studentId}/notebook")
    @PreAuthorize("hasRole('TEACHER')")
    public NotesOverviewResponse getStudentNotebook(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getStudentNotebook(id, studentId, principal);
    }

    /**
     * Deletes a student's note from their notebook (teacher action).
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param noteId    note UUID to delete
     * @param principal the authenticated teacher
     */
    @DeleteMapping("/{id}/students/{studentId}/notebook/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteStudentNote(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @PathVariable UUID noteId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.deleteStudentNote(id, studentId, noteId, principal);
    }

    /**
     * Returns all medalions (achievement badges) earned by a student in this classroom.
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param principal the authenticated teacher
     * @return list of earned medalions
     */
    @GetMapping("/{id}/students/{studentId}/medalions")
    @PreAuthorize("hasRole('TEACHER')")
    public List<MedalionResponse> getStudentMedalions(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.getStudentMedalions(id, studentId, principal);
    }

    /**
     * Changes the display username of a student within this classroom.
     *
     * @param id        classroom UUID
     * @param studentId student UUID
     * @param request   new username
     * @param principal the authenticated teacher
     */
    @PatchMapping("/{id}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void updateStudentUsername(
            @PathVariable UUID id,
            @PathVariable UUID studentId,
            @Valid @RequestBody UpdateStudentUsernameRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.updateStudentUsername(id, studentId, request, principal);
    }


    /**
     * Creates a new classroom owned by the authenticated teacher.
     *
     * @param request   classroom creation parameters
     * @param principal the authenticated teacher
     * @return the newly created classroom
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public ClassroomResponse createClassroom(
            @Valid @RequestBody CreateClassroomRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.createClassroom(request, principal.getUserId());
    }

    // TODO: rework implementation
//    @GetMapping("/notifications")
//    @PreAuthorize("hasRole('TEACHER')")
//    public List<NotificationResponse> getNotifications(
//        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
//        return teacherClassroomService.getNotifications(principal.getUserId());
//    }

    /**
     * Lists all teachers (owner + co-teachers) associated with the classroom.
     *
     * @param id        classroom UUID
     * @param principal the authenticated teacher
     * @return list of co-teacher records
     */
    @GetMapping("/{id}/teachers")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CoTeacherResponse> listCoTeachers(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.listCoTeachers(id, principal);
    }

    /**
     * Invites another teacher to co-teach this classroom by email.
     *
     * @param id        classroom UUID
     * @param request   email of the teacher to invite
     * @param principal the authenticated teacher; must be the owner
     * @return the newly created co-teacher record
     * @throws org.springframework.web.server.ResponseStatusException 403 if the caller is not the owner
     */
    @PostMapping("/{id}/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public CoTeacherResponse inviteCoTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody InviteCoTeacherRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherClassroomService.inviteCoTeacher(id, request, principal);
    }

    /**
     * Removes a co-teacher from the classroom.
     *
     * @param id        classroom UUID
     * @param userId    UUID of the co-teacher to remove
     * @param principal the authenticated teacher; must be the owner
     */
    @DeleteMapping("/{id}/teachers/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void removeCoTeacher(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        teacherClassroomService.removeCoTeacher(id, userId, principal);
    }

    /**
     * Returns all profanity/censor words configured for this classroom's mystery submissions.
     *
     * @param id        classroom UUID
     * @param principal the authenticated teacher
     * @return list of censor-word entries
     */
    @GetMapping("/{id}/censor-words")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CensorWordResponse> listCensorWords(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return censorWordService.listWords(id, principal);
    }

    /**
     * Adds a new word to the classroom's profanity filter.
     *
     * @param id        classroom UUID
     * @param request   the word to block
     * @param principal the authenticated teacher
     * @return the created censor-word entry
     */
    @PostMapping("/{id}/censor-words")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public CensorWordResponse addCensorWord(
            @PathVariable UUID id,
            @Valid @RequestBody AddCensorWordRequest request,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return censorWordService.addWord(id, request, principal);
    }

    /**
     * Removes a word from the classroom's profanity filter.
     *
     * @param id        classroom UUID
     * @param wordId    UUID of the censor-word entry to remove
     * @param principal the authenticated teacher
     */
    @DeleteMapping("/{id}/censor-words/{wordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteCensorWord(
            @PathVariable UUID id,
            @PathVariable UUID wordId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        censorWordService.deleteWord(id, wordId, principal);
    }
}
