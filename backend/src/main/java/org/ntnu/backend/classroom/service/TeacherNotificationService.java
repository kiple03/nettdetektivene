package org.ntnu.backend.classroom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.game.repository.LevelCompletionRepository;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates recent student events across all classrooms owned by a teacher
 * into a single activity feed.
 */
@Service
@RequiredArgsConstructor
public class TeacherNotificationService {

    private final LevelCompletionRepository levelCompletionRepository;
    private final MysterySubmissionRepository mysterySubmissionRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID teacherId) {
        List<NotificationResponse> notifications = new ArrayList<>();

        levelCompletionRepository.findRecentByTeacherId(teacherId)
            .forEach(lc -> {
                var enrollment = lc.getEnrollment();
                var username = enrollment.getUser().getUsername();
                var classroom = enrollment.getClassroom();
                notifications.add(NotificationResponse.builder()
                    .type("LEVEL_COMPLETED")
                    .message(username + " completed a level in " + classroom.getTitle())
                    .classroomId(classroom.getId().toString())
                    .studentUsername(username)
                    .timestamp(lc.getCompletedAt())
                    .build());
            });

        mysterySubmissionRepository.findRecentByTeacherId(teacherId)
            .forEach(ms -> {
                var enrollment = ms.getEnrollment();
                var username = enrollment.getUser().getUsername();
                var classroom = enrollment.getClassroom();
                notifications.add(NotificationResponse.builder()
                    .type("MYSTERY_SUBMISSION")
                    .message(username + " submitted a mystery answer in " + classroom.getTitle())
                    .classroomId(classroom.getId().toString())
                    .studentUsername(username)
                    .timestamp(ms.getSubmittedAt())
                    .build());
            });

        classroomTeacherRepository.findNonOwnerEntriesByUserId(teacherId).stream()
            .filter(ct -> ct.getCreatedAt() != null)
            .forEach(ct -> {
                var classroom = ct.getClassroom();
                var inviterUsername = classroomTeacherRepository
                    .findOwnerWithUserByClassroomId(classroom.getId())
                    .map(owner -> owner.getUser().getUsername())
                    .orElse("ukjent");
                notifications.add(NotificationResponse.builder()
                    .type("CO_TEACHER_ADDED")
                    .message("Du ble lagt til som medlærer i " + classroom.getTitle())
                    .classroomId(classroom.getId().toString())
                    .studentUsername(inviterUsername)
                    .timestamp(ct.getCreatedAt())
                    .build());
            });

        notifications.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return notifications;
    }
}
