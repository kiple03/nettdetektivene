package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.dto.BossBattleResultResponse;
import org.ntnu.backend.game.dto.BossBattleSaveRequest;
import org.ntnu.backend.game.model.BossBattleResult;
import org.ntnu.backend.game.repository.BossBattleResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BossBattleServiceTest {

    @Mock
    private BossBattleResultRepository bossBattleResultRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private BossBattleService bossBattleService;

    @Test
    void saveResult_whenUserIsEnrolled_shouldSaveBossBattleResult() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        Enrollment enrollment = mock(Enrollment.class);
        BossBattleSaveRequest request = new BossBattleSaveRequest(true, 120, 45);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId))
                .thenReturn(Optional.of(enrollment));

        bossBattleService.saveResult(userId, classroomId, request);

        ArgumentCaptor<BossBattleResult> captor = ArgumentCaptor.forClass(BossBattleResult.class);
        verify(bossBattleResultRepository).save(captor.capture());

        BossBattleResult saved = captor.getValue();
        assertThat(saved.getEnrollment()).isEqualTo(enrollment);
        assertThat(saved.isWon()).isTrue();
        assertThat(saved.getTimeSeconds()).isEqualTo(120);
        assertThat(saved.getPlayerHpRemaining()).isEqualTo(45);
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    @Test
    void saveResult_whenClassroomIdIsNull_shouldThrowForbiddenAndNotSave() {
        UUID userId = UUID.randomUUID();
        BossBattleSaveRequest request = new BossBattleSaveRequest(false, 80, 0);

        assertThatThrownBy(() -> bossBattleService.saveResult(userId, null, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getReason()).isEqualTo("No classroom assigned");
                });

        verifyNoInteractions(enrollmentRepository);
        verifyNoInteractions(bossBattleResultRepository);
    }

    @Test
    void saveResult_whenUserIsNotEnrolled_shouldThrowForbiddenAndNotSave() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        BossBattleSaveRequest request = new BossBattleSaveRequest(false, 80, 0);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bossBattleService.saveResult(userId, classroomId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getReason()).isEqualTo("Not enrolled in classroom");
                });

        verify(bossBattleResultRepository, never()).save(any());
    }

    @Test
    void getLatestResult_whenResultExists_shouldReturnMappedResponse() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Instant completedAt = Instant.parse("2026-04-28T10:00:00Z");

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getId()).thenReturn(enrollmentId);

        BossBattleResult result = mock(BossBattleResult.class);
        when(result.isWon()).thenReturn(true);
        when(result.getTimeSeconds()).thenReturn(95);
        when(result.getPlayerHpRemaining()).thenReturn(30);
        when(result.getCompletedAt()).thenReturn(completedAt);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId))
                .thenReturn(Optional.of(enrollment));
        when(bossBattleResultRepository.findTopByEnrollmentIdOrderByCompletedAtDesc(enrollmentId))
                .thenReturn(Optional.of(result));

        Optional<BossBattleResultResponse> response =
                bossBattleService.getLatestResult(userId, classroomId);

        assertThat(response).isPresent();
        assertThat(response.get().won()).isTrue();
        assertThat(response.get().timeSeconds()).isEqualTo(95);
        assertThat(response.get().playerHpRemaining()).isEqualTo(30);
        assertThat(response.get().completedAt()).isEqualTo(completedAt);
    }

    @Test
    void getLatestResult_whenNoResultExists_shouldReturnEmpty() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        Enrollment enrollment = mock(Enrollment.class);
        when(enrollment.getId()).thenReturn(enrollmentId);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId))
                .thenReturn(Optional.of(enrollment));
        when(bossBattleResultRepository.findTopByEnrollmentIdOrderByCompletedAtDesc(enrollmentId))
                .thenReturn(Optional.empty());

        Optional<BossBattleResultResponse> response =
                bossBattleService.getLatestResult(userId, classroomId);

        assertThat(response).isEmpty();
    }

    @Test
    void getLatestResult_whenUserIsNotEnrolled_shouldThrowForbidden() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bossBattleService.getLatestResult(userId, classroomId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getReason()).isEqualTo("Not enrolled in classroom");
                });

        verifyNoInteractions(bossBattleResultRepository);
    }
}