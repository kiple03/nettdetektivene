package org.ntnu.backend.classroom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.NotificationResponse;
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

        notifications.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return notifications;
    }
}
