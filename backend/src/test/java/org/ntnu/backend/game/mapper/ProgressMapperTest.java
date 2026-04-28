package org.ntnu.backend.game.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.StopLevelCount;

class ProgressMapperTest {

    private ProgressMapper progressMapper;

    @BeforeEach
    void setUp() {
        progressMapper = new ProgressMapper();
    }

    @Test
    void toSummary_whenStudentIsAtFirstLevelOfFirstStop_shouldReturnZeroCompletedLevels() {
        UUID userId = UUID.randomUUID();

        Stop firstStop = stop(1, "Nyhetskvartalet", 1);
        UserProgress progress = progress(userId, firstStop, 1, false);

        List<StopLevelCount> stopLevelCounts = List.of(
                stopLevelCount(1, 1, 3),
                stopLevelCount(2, 2, 2)
        );

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, stopLevelCounts, 5);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isZero();
        assertThat(result.currentStopDisplayOrder()).isEqualTo(1);
        assertThat(result.currentStopName()).isEqualTo("Nyhetskvartalet");
        assertThat(result.currentLevelOrder()).isEqualTo(1);
        assertThat(result.gameCompleted()).isFalse();
    }

    @Test
    void toSummary_whenStudentHasAdvancedWithinCurrentStop_shouldCountPreviousLevelsInSameStop() {
        UUID userId = UUID.randomUUID();

        Stop firstStop = stop(1, "Nyhetskvartalet", 1);
        UserProgress progress = progress(userId, firstStop, 3, false);

        List<StopLevelCount> stopLevelCounts = List.of(
                stopLevelCount(1, 1, 4),
                stopLevelCount(2, 2, 2)
        );

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, stopLevelCounts, 6);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isEqualTo(2);
        assertThat(result.currentStopDisplayOrder()).isEqualTo(1);
        assertThat(result.currentStopName()).isEqualTo("Nyhetskvartalet");
        assertThat(result.currentLevelOrder()).isEqualTo(3);
        assertThat(result.gameCompleted()).isFalse();
    }

    @Test
    void toSummary_whenStudentIsAtLaterStop_shouldCountLevelsFromPreviousStopsAndCurrentProgress() {
        UUID userId = UUID.randomUUID();

        Stop thirdStop = stop(3, "Postkontoret", 3);
        UserProgress progress = progress(userId, thirdStop, 2, false);

        List<StopLevelCount> stopLevelCounts = List.of(
                stopLevelCount(1, 1, 3),
                stopLevelCount(2, 2, 2),
                stopLevelCount(3, 3, 4),
                stopLevelCount(4, 4, 1)
        );

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, stopLevelCounts, 10);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isEqualTo(6);
        assertThat(result.currentStopDisplayOrder()).isEqualTo(3);
        assertThat(result.currentStopName()).isEqualTo("Postkontoret");
        assertThat(result.currentLevelOrder()).isEqualTo(2);
        assertThat(result.gameCompleted()).isFalse();
    }

    @Test
    void toSummary_whenGameCompleted_shouldReturnTotalLevelsInGame() {
        UUID userId = UUID.randomUUID();

        Stop lastStop = stop(4, "Bosskamp", 4);
        UserProgress progress = progress(userId, lastStop, 3, true);

        List<StopLevelCount> stopLevelCounts = List.of(
                stopLevelCount(1, 1, 3),
                stopLevelCount(2, 2, 2),
                stopLevelCount(3, 3, 4),
                stopLevelCount(4, 4, 3)
        );

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, stopLevelCounts, 12);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isEqualTo(12);
        assertThat(result.currentStopDisplayOrder()).isEqualTo(4);
        assertThat(result.currentStopName()).isEqualTo("Bosskamp");
        assertThat(result.currentLevelOrder()).isEqualTo(3);
        assertThat(result.gameCompleted()).isTrue();
    }

    @Test
    void toSummary_whenStopLevelCountsIsEmptyAndGameNotCompleted_shouldUseOnlyCurrentLevelOrder() {
        UUID userId = UUID.randomUUID();

        Stop stop = stop(2, "Fotografen", 2);
        UserProgress progress = progress(userId, stop, 2, false);

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, List.of(), 0);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isEqualTo(1);
        assertThat(result.currentStopDisplayOrder()).isEqualTo(2);
        assertThat(result.currentStopName()).isEqualTo("Fotografen");
        assertThat(result.currentLevelOrder()).isEqualTo(2);
        assertThat(result.gameCompleted()).isFalse();
    }

    @Test
    void toSummary_whenStopLevelCountsAreNotSorted_shouldStillCountByDisplayOrder() {
        UUID userId = UUID.randomUUID();

        Stop thirdStop = stop(3, "Postkontoret", 3);
        UserProgress progress = progress(userId, thirdStop, 1, false);

        List<StopLevelCount> stopLevelCounts = List.of(
                stopLevelCount(3, 3, 4),
                stopLevelCount(1, 1, 3),
                stopLevelCount(2, 2, 2)
        );

        ProgressSummaryResponse result =
                progressMapper.toSummary(progress, stopLevelCounts, 9);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalLevelsCompleted()).isEqualTo(5);
        assertThat(result.currentStopDisplayOrder()).isEqualTo(3);
        assertThat(result.currentStopName()).isEqualTo("Postkontoret");
        assertThat(result.currentLevelOrder()).isEqualTo(1);
        assertThat(result.gameCompleted()).isFalse();
    }

    private UserProgress progress(UUID userId, Stop currentStop, int currentLevelOrder, boolean gameCompleted) {
        UserProgress progress = new UserProgress();
        progress.setId(userId);
        progress.setCurrentStop(currentStop);
        progress.setCurrentLevelOrder(currentLevelOrder);
        progress.setGameCompleted(gameCompleted);
        return progress;
    }

    private Stop stop(int id, String name, int displayOrder) {
        Stop stop = new Stop();
        stop.setId(id);
        stop.setName(name);
        stop.setDisplayOrder(displayOrder);
        return stop;
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