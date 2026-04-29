package org.ntnu.backend.classroom.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;

class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void toEntity_shouldMapFieldsAndSetReadToFalse() {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        Notification result = mapper.toEntity(
                teacherId,
                classroomId,
                studentId,
                NotificationType.AREA_COMPLETED,
                "Student completed an area"
        );

        assertThat(result.getTeacherId()).isEqualTo(teacherId);
        assertThat(result.getClassroomId()).isEqualTo(classroomId);
        assertThat(result.getStudentId()).isEqualTo(studentId);
        assertThat(result.getType()).isEqualTo(NotificationType.AREA_COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Student completed an area");
        assertThat(result.isRead()).isFalse();
    }

    @Test
    void toEntity_whenIdsAreNull_shouldKeepNullValues() {
        Notification result = mapper.toEntity(
                null,
                null,
                null,
                NotificationType.INAPPROPRIATE_NOTE,
                "Message"
        );

        assertThat(result.getTeacherId()).isNull();
        assertThat(result.getClassroomId()).isNull();
        assertThat(result.getStudentId()).isNull();
        assertThat(result.getType()).isEqualTo(NotificationType.INAPPROPRIATE_NOTE);
        assertThat(result.getMessage()).isEqualTo("Message");
        assertThat(result.isRead()).isFalse();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID notificationId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-04-28T10:00:00Z");

        Notification notification = Notification.builder()
                .id(notificationId)
                .teacherId(teacherId)
                .classroomId(classroomId)
                .studentId(studentId)
                .type(NotificationType.STUDENT_STUCK)
                .message("Student has been stuck")
                .read(true)
                .createdAt(createdAt)
                .build();

        NotificationResponse result = mapper.toResponse(notification);

        assertThat(result.getId()).isEqualTo(notificationId);
        assertThat(result.getTeacherId()).isEqualTo(teacherId);
        assertThat(result.getClassroomId()).isEqualTo(classroomId);
        assertThat(result.getStudentId()).isEqualTo(studentId);
        assertThat(result.getType()).isEqualTo(NotificationType.STUDENT_STUCK);
        assertThat(result.getMessage()).isEqualTo("Student has been stuck");
        assertThat(result.isRead()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toResponse_whenNotificationIsUnread_shouldMapReadFalse() {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .teacherId(UUID.randomUUID())
                .classroomId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .type(NotificationType.GAME_COMPLETED)
                .message("Student completed the game")
                .read(false)
                .createdAt(Instant.parse("2026-04-28T10:00:00Z"))
                .build();

        NotificationResponse result = mapper.toResponse(notification);

        assertThat(result.isRead()).isFalse();
        assertThat(result.getType()).isEqualTo(NotificationType.GAME_COMPLETED);
        assertThat(result.getMessage()).isEqualTo("Student completed the game");
    }
}