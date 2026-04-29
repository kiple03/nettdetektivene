package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.exception.UserProgressNotFoundException;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.game.mapper.ProgressMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private ProgressMapper progressMapper;

    @Mock
    private MedalionService medalionService;

    @InjectMocks
    private ProgressService progressService;

    private UUID userId;
    private Stop stopOne;
    private Stop stopTwo;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        stopOne = stop(1, "Nyhetskvartalet", 1);
        stopTwo = stop(2, "Fotografen", 2);
    }

    @Test
    void initProgressForStudent_shouldCreateInitialProgress() {
        User student = user(userId, RoleEnum.STUDENT);
        when(stopRepository.findFirstStop()).thenReturn(Optional.of(stopOne));

        progressService.initProgressForStudent(student);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(userProgressRepository).save(captor.capture());

        UserProgress saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(student);
        assertThat(saved.getCurrentStop()).isEqualTo(stopOne);
        assertThat(saved.getCurrentLevelOrder()).isEqualTo(1);
        assertThat(saved.isGameCompleted()).isFalse();
        assertThat(saved.getLastProgressAt()).isNotNull();
    }

    @Test
    void initProgressForStudent_whenUserIsTeacher_shouldThrow() {
        User teacher = user(UUID.randomUUID(), RoleEnum.TEACHER);

        assertThatThrownBy(() -> progressService.initProgressForStudent(teacher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Should not try to create progress for teacher");

        verifyNoInteractions(stopRepository);
        verifyNoInteractions(userProgressRepository);
    }

    @Test
    void initProgressForStudent_whenFirstStopMissing_shouldThrow() {
        User student = user(userId, RoleEnum.STUDENT);
        when(stopRepository.findFirstStop()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.initProgressForStudent(student))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First stop not configured");

        verify(userProgressRepository, never()).save(any());
    }

    @Test
    void isStopAccessible_whenTeacher_shouldReturnTrueWithoutProgressLookup() {
        boolean result = progressService.isStopAccessible(userId, RoleEnum.TEACHER, stopTwo);

        assertThat(result).isTrue();
        verifyNoInteractions(userProgressRepository);
    }

    @Test
    void isStopAccessible_whenStudentAndStopIsBeforeOrCurrent_shouldReturnTrue() {
        UserProgress progress = progress(userId, stopTwo, 1, false);
        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stopOne)).isTrue();
        assertThat(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stopTwo)).isTrue();
    }

    @Test
    void isStopAccessible_whenStudentAndStopIsAfterCurrent_shouldReturnFalse() {
        UserProgress progress = progress(userId, stopOne, 1, false);
        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stopTwo)).isFalse();
    }

    @Test
    void isStopAccessible_whenProgressMissing_shouldThrow() {
        when(userProgressRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.isStopAccessible(userId, RoleEnum.STUDENT, stopOne))
                .isInstanceOf(UserProgressNotFoundException.class);
    }

    @Test
    void isLevelAccessible_whenTeacher_shouldReturnTrueWithoutProgressLookup() {
        Level lockedLevel = level(10, stopTwo, 5);

        boolean result = progressService.isLevelAccessible(userId, RoleEnum.TEACHER, lockedLevel);

        assertThat(result).isTrue();
        verifyNoInteractions(userProgressRepository);
    }

    @Test
    void isLevelAccessible_whenLevelIsInPastStop_shouldReturnTrue() {
        UserProgress progress = progress(userId, stopTwo, 1, false);
        Level previousLevel = level(1, stopOne, 99);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, previousLevel)).isTrue();
    }

    @Test
    void isLevelAccessible_whenLevelIsInFutureStop_shouldReturnFalse() {
        UserProgress progress = progress(userId, stopOne, 2, false);
        Level futureLevel = level(5, stopTwo, 1);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, futureLevel)).isFalse();
    }

    @Test
    void isLevelAccessible_whenSameStopAndLevelOrderIsUnlocked_shouldReturnTrue() {
        UserProgress progress = progress(userId, stopOne, 2, false);
        Level currentLevel = level(2, stopOne, 2);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, currentLevel)).isTrue();
    }

    @Test
    void isLevelAccessible_whenSameStopAndLevelOrderIsLocked_shouldReturnFalse() {
        UserProgress progress = progress(userId, stopOne, 2, false);
        Level lockedLevel = level(3, stopOne, 3);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        assertThat(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, lockedLevel)).isFalse();
    }

    @Test
    void recordCompletionOfLevel_whenTeacher_shouldReturnFalse() {
        Level level = level(1, stopOne, 1);

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.TEACHER, level);

        assertThat(result).isFalse();
        verifyNoInteractions(userProgressRepository);
        verifyNoInteractions(levelRepository);
        verifyNoInteractions(stopRepository);
        verifyNoInteractions(medalionService);
    }

    @Test
    void recordCompletionOfLevel_whenReplayOrOutOfOrder_shouldReturnFalseAndNotAdvance() {
        UserProgress progress = progress(userId, stopOne, 2, false);
        Level replayedLevel = level(1, stopOne, 1);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, replayedLevel);

        assertThat(result).isFalse();
        assertThat(progress.getCurrentStop()).isEqualTo(stopOne);
        assertThat(progress.getCurrentLevelOrder()).isEqualTo(2);
        assertThat(progress.isGameCompleted()).isFalse();

        verifyNoInteractions(levelRepository);
        verifyNoInteractions(stopRepository);
        verifyNoInteractions(medalionService);
    }

    @Test
    void recordCompletionOfLevel_whenMoreLevelsRemainInCurrentStop_shouldIncrementLevelOrder() {
        UserProgress progress = progress(userId, stopOne, 1, false);
        Level currentLevel = level(1, stopOne, 1);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));
        when(levelRepository.countByStopId(stopOne.getId())).thenReturn(3L);

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, currentLevel);

        assertThat(result).isTrue();
        assertThat(progress.getCurrentStop()).isEqualTo(stopOne);
        assertThat(progress.getCurrentLevelOrder()).isEqualTo(2);
        assertThat(progress.isGameCompleted()).isFalse();

        verify(stopRepository, never()).findNextStopAfter(anyInt());
        verifyNoInteractions(medalionService);
    }

    @Test
    void recordCompletionOfLevel_whenLastLevelInStopAndNextStopExists_shouldMoveToNextStopAndAwardStopMedal() {
        UserProgress progress = progress(userId, stopOne, 3, false);
        Level currentLevel = level(3, stopOne, 3);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));
        when(levelRepository.countByStopId(stopOne.getId())).thenReturn(3L);
        when(stopRepository.findNextStopAfter(stopOne.getDisplayOrder())).thenReturn(Optional.of(stopTwo));

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, currentLevel);

        assertThat(result).isTrue();
        assertThat(progress.getCurrentStop()).isEqualTo(stopTwo);
        assertThat(progress.getCurrentLevelOrder()).isEqualTo(1);
        assertThat(progress.isGameCompleted()).isFalse();

        verify(medalionService).awardInternalUsage(
                userId,
                "Breaking news",
                "Du avslørte alle falske nyheter"
        );
    }

    @Test
    void recordCompletionOfLevel_whenLastLevelInLastStop_shouldMarkGameCompletedAndAwardPlatinum() {
        UserProgress progress = progress(userId, stopTwo, 2, false);
        Level currentLevel = level(7, stopTwo, 2);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));
        when(levelRepository.countByStopId(stopTwo.getId())).thenReturn(2L);
        when(stopRepository.findNextStopAfter(stopTwo.getDisplayOrder())).thenReturn(Optional.empty());

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, currentLevel);

        assertThat(result).isTrue();
        assertThat(progress.getCurrentStop()).isEqualTo(stopTwo);
        assertThat(progress.getCurrentLevelOrder()).isEqualTo(2);
        assertThat(progress.isGameCompleted()).isTrue();

        verify(medalionService).awardInternalUsage(
                userId,
                "platinum",
                "Du fanget skurken"
        );
    }

    @Test
    void recordCompletionOfLevel_whenGameAlreadyCompleted_shouldReturnFalse() {
        UserProgress progress = progress(userId, stopTwo, 2, true);
        Level level = level(7, stopTwo, 2);

        when(userProgressRepository.findById(userId)).thenReturn(Optional.of(progress));

        boolean result = progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, level);

        assertThat(result).isFalse();
        verifyNoInteractions(levelRepository);
        verifyNoInteractions(stopRepository);
        verifyNoInteractions(medalionService);
    }

    @Test
    void getProgressForStudents_shouldReturnProgressSummariesMappedByProgressId() {
        UUID userOne = UUID.randomUUID();
        UUID userTwo = UUID.randomUUID();

        UserProgress progressOne = progress(userOne, stopOne, 1, false);
        UserProgress progressTwo = progress(userTwo, stopTwo, 2, false);

        StopLevelCount countOne = stopLevelCount(1, 1, 3);
        StopLevelCount countTwo = stopLevelCount(2, 2, 2);

        ProgressSummaryResponse responseOne =
                new ProgressSummaryResponse(userOne, 0, 1, "Nyhetskvartalet", 1, false);
        ProgressSummaryResponse responseTwo =
                new ProgressSummaryResponse(userTwo, 4, 2, "Fotografen", 2, false);

        when(stopRepository.findAllWithLevelCounts()).thenReturn(List.of(countOne, countTwo));
        when(userProgressRepository.findByUserIdIn(List.of(userOne, userTwo)))
                .thenReturn(List.of(progressOne, progressTwo));

        when(progressMapper.toSummary(progressOne, List.of(countOne, countTwo), 5))
                .thenReturn(responseOne);
        when(progressMapper.toSummary(progressTwo, List.of(countOne, countTwo), 5))
                .thenReturn(responseTwo);

        Map<UUID, ProgressSummaryResponse> result =
                progressService.getProgressForStudents(List.of(userOne, userTwo));

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(userOne, responseOne);
        assertThat(result).containsEntry(userTwo, responseTwo);
    }

    private User user(UUID id, RoleEnum role) {
        return User.builder()
                .id(id)
                .email("test@example.com")
                .role(role)
                .build();
    }

    private Stop stop(int id, String name, int displayOrder) {
        Stop stop = new Stop();
        stop.setId(id);
        stop.setName(name);
        stop.setDisplayOrder(displayOrder);
        return stop;
    }

    private Level level(int id, Stop stop, int order) {
        Level level = new Level();
        level.setId(id);
        level.setStop(stop);
        level.setOrder(order);
        level.setTitle("Level " + order);
        return level;
    }

    private UserProgress progress(UUID id, Stop currentStop, int currentLevelOrder, boolean gameCompleted) {
        UserProgress progress = new UserProgress();
        progress.setId(id);
        progress.setUser(user(id, RoleEnum.STUDENT));
        progress.setCurrentStop(currentStop);
        progress.setCurrentLevelOrder(currentLevelOrder);
        progress.setGameCompleted(gameCompleted);
        return progress;
    }

    private StopLevelCount stopLevelCount(int stopId, int displayOrder, long levelCount) {
        return new StopLevelCount() {
            @Override
            public int getStopId() {
                return stopId;
            }

            @Override
            public int getDisplayOrder() {
                return displayOrder;
            }

            @Override
            public long getLevelCount() {
                return levelCount;
            }
        };
    }
}