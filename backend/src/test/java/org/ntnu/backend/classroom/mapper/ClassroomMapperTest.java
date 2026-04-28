package org.ntnu.backend.classroom.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.user.model.User;

class ClassroomMapperTest {

    private ClassroomMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClassroomMapper();
    }

    @Test
    void toResponse_shouldMapClassroomToResponse() {
        UUID classroomId = UUID.randomUUID();

        Classroom classroom = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .description("Beskrivelse")
                .joinCode("ABC123")
                .build();

        ClassroomResponse result = mapper.toResponse(classroom);

        assertThat(result.getId()).isEqualTo(classroomId.toString());
        assertThat(result.getTitle()).isEqualTo("Klasse 5A");
        assertThat(result.getDescription()).isEqualTo("Beskrivelse");
        assertThat(result.getJoinCode()).isEqualTo("ABC123");
    }

    @Test
    void toEntity_shouldTrimTitleAndDescriptionAndSetJoinCode() {
        CreateClassroomRequest request = new CreateClassroomRequest();
        request.setTitle("  Klasse 5A  ");
        request.setDescription("  Beskrivelse  ");

        Classroom result = mapper.toEntity(request, "ABC123");

        assertThat(result.getTitle()).isEqualTo("Klasse 5A");
        assertThat(result.getDescription()).isEqualTo("Beskrivelse");
        assertThat(result.getJoinCode()).isEqualTo("ABC123");
    }

    @Test
    void toEntity_whenDescriptionIsNull_shouldKeepDescriptionNull() {
        CreateClassroomRequest request = new CreateClassroomRequest();
        request.setTitle("  Klasse 5A  ");
        request.setDescription(null);

        Classroom result = mapper.toEntity(request, "ABC123");

        assertThat(result.getTitle()).isEqualTo("Klasse 5A");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getJoinCode()).isEqualTo("ABC123");
    }

    @Test
    void applyUpdates_shouldUpdateTitleAndDescriptionWhenProvided() {
        Classroom classroom = Classroom.builder()
                .title("Old title")
                .description("Old description")
                .build();

        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("  New title  ");
        request.setDescription("  New description  ");

        mapper.applyUpdates(classroom, request);

        assertThat(classroom.getTitle()).isEqualTo("New title");
        assertThat(classroom.getDescription()).isEqualTo("New description");
    }

    @Test
    void applyUpdates_whenTitleIsBlank_shouldKeepOldTitle() {
        Classroom classroom = Classroom.builder()
                .title("Old title")
                .description("Old description")
                .build();

        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("   ");
        request.setDescription("New description");

        mapper.applyUpdates(classroom, request);

        assertThat(classroom.getTitle()).isEqualTo("Old title");
        assertThat(classroom.getDescription()).isEqualTo("New description");
    }

    @Test
    void applyUpdates_whenDescriptionIsBlank_shouldSetDescriptionToNull() {
        Classroom classroom = Classroom.builder()
                .title("Old title")
                .description("Old description")
                .build();

        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle(null);
        request.setDescription("   ");

        mapper.applyUpdates(classroom, request);

        assertThat(classroom.getTitle()).isEqualTo("Old title");
        assertThat(classroom.getDescription()).isNull();
    }

    @Test
    void applyUpdates_whenDescriptionIsNull_shouldKeepOldDescription() {
        Classroom classroom = Classroom.builder()
                .title("Old title")
                .description("Old description")
                .build();

        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle(null);
        request.setDescription(null);

        mapper.applyUpdates(classroom, request);

        assertThat(classroom.getTitle()).isEqualTo("Old title");
        assertThat(classroom.getDescription()).isEqualTo("Old description");
    }

    @Test
    void toSummary_shouldMapClassroomAndCalculateProgressPercent() {
        UUID classroomId = UUID.randomUUID();

        Instant instant = Instant.parse("2026-04-28T10:00:00Z");
        LocalDateTime createdAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        Classroom classroom = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .description("Beskrivelse")
                .joinCode("ABC123")
                .createdAt(createdAt)
                .build();

        ClassroomSummaryResponse result = mapper.toSummary(
                classroom,
                12,
                5,
                2.5,
                450
        );

        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toSummary_whenTotalStopsIsZero_shouldSetProgressPercentToZero() {
        Classroom classroom = Classroom.builder()
                .id(UUID.randomUUID())
                .title("Klasse 5A")
                .description("Beskrivelse")
                .joinCode("ABC123")
                .build();

        ClassroomSummaryResponse result = mapper.toSummary(
                classroom,
                0,
                0,
                3.0,
                0
        );

        assertThat(result.getProgressPercent()).isZero();
    }

    @Test
    void toStudentProgress_whenProgressExists_shouldMapProgressAndStudentData() {
        User user = user("Anna", "anna@example.com");
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .build();

        ProgressSummaryResponse progress = new ProgressSummaryResponse(
                user.getId(),
                4,
                2,
                "Fotografen",
                1,
                false
        );

        List<EquippedItemDto> equippedItems = List.of();

        StudentProgressResponse result = mapper.toStudentProgress(
                enrollment,
                progress,
                500,
                3,
                6,
                equippedItems
        );

        assertThat(result.getUserId()).isEqualTo(user.getId().toString());
        assertThat(result.getUsername()).isEqualTo("Anna");
        assertThat(result.getEmail()).isEqualTo("anna@example.com");
        assertThat(result.getCurrentStopDisplayOrder()).isEqualTo(2);
        assertThat(result.getTotalStops()).isEqualTo(6);
        assertThat(result.getScore()).isEqualTo(500);
        assertThat(result.getMedalsEarned()).isEqualTo(3);
        assertThat(result.getEquippedItems()).isEqualTo(equippedItems);
    }

    @Test
    void toStudentProgress_whenProgressIsNull_shouldUseZeroAsCurrentStopDisplayOrder() {
        User user = user("Anna", "anna@example.com");
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .build();

        StudentProgressResponse result = mapper.toStudentProgress(
                enrollment,
                null,
                0,
                0,
                6,
                List.of()
        );

        assertThat(result.getUserId()).isEqualTo(user.getId().toString());
        assertThat(result.getUsername()).isEqualTo("Anna");
        assertThat(result.getEmail()).isEqualTo("anna@example.com");
        assertThat(result.getCurrentStopDisplayOrder()).isZero();
        assertThat(result.getTotalStops()).isEqualTo(6);
        assertThat(result.getScore()).isZero();
        assertThat(result.getMedalsEarned()).isZero();
    }

    @Test
    void toStudentDetail_whenProgressExists_shouldMapProgressFields() {
        User user = user("Anna", "anna@example.com");
        Instant enrolledAt = Instant.parse("2026-04-28T10:00:00Z");

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .enrolledAt(enrolledAt)
                .build();

        ProgressSummaryResponse progress = new ProgressSummaryResponse(
                user.getId(),
                7,
                3,
                "Postkontoret",
                2,
                true
        );

        StudentDetailResponse result = mapper.toStudentDetail(enrollment, progress);

        assertThat(result.getUsername()).isEqualTo("Anna");
        assertThat(result.getEmail()).isEqualTo("anna@example.com");
        assertThat(result.getEnrolledAt()).isEqualTo(enrolledAt);
        assertThat(result.getCurrentStopName()).isEqualTo("Postkontoret");
        assertThat(result.getCurrentLevelOrder()).isEqualTo(2);
        assertThat(result.getTotalLevelsCompleted()).isEqualTo(7);
        assertThat(result.isGameCompleted()).isTrue();
    }

    @Test
    void toStudentDetail_whenProgressIsNull_shouldOnlyMapStudentBaseFields() {
        User user = user("Anna", "anna@example.com");
        Instant enrolledAt = Instant.parse("2026-04-28T10:00:00Z");

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .enrolledAt(enrolledAt)
                .build();

        StudentDetailResponse result = mapper.toStudentDetail(enrollment, null);

        assertThat(result.getUsername()).isEqualTo("Anna");
        assertThat(result.getEmail()).isEqualTo("anna@example.com");
        assertThat(result.getEnrolledAt()).isEqualTo(enrolledAt);
        assertThat(result.getCurrentStopName()).isNull();
        assertThat(result.getCurrentLevelOrder()).isEqualTo(0);
        assertThat(result.getTotalLevelsCompleted()).isEqualTo(0);
        assertThat(result.isGameCompleted()).isFalse();
    }

    private User user(String username, String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .build();
    }
}