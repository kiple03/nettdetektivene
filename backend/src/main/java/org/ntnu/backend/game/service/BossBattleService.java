package org.ntnu.backend.game.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.exception.ApiException;
import org.ntnu.backend.exception.BossLockedException;
import org.ntnu.backend.game.dto.BossBattleResultResponse;
import org.ntnu.backend.game.dto.BossBattleSaveRequest;
import org.ntnu.backend.game.model.BossBattleResult;
import org.ntnu.backend.game.repository.BossBattleResultRepository;
import org.ntnu.backend.medalion.service.MedalionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Handles saving and retrieving boss battle results, including awarding medals and unlocking avatar items on victory. */
@Service
@RequiredArgsConstructor
public class BossBattleService {

    private final BossBattleResultRepository bossBattleResultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MedalionService medalionService;
    private final ProgressService progressService;
    private final UserAvatarService userAvatarService;

    /**
     * Persists the outcome of a boss battle and, on victory, awards a Platinum medallion and unlocks the BOSS_DEFEAT avatar item.
     *
     * @throws BossLockedException if the user has not yet unlocked the boss fight
     * @throws ResponseStatusException 403 if the user is not enrolled in the given classroom
     */
    @Transactional
    public void saveResult(UUID userId, UUID classroomId, BossBattleSaveRequest request) {
        if (!progressService.boosFightUnlocked(userId)) {
            throw new BossLockedException();
        }

        Enrollment enrollment = resolveEnrollment(userId, classroomId);
        BossBattleResult result = BossBattleResult.builder()
            .enrollment(enrollment)
            .won(request.won())
            .timeSeconds(request.timeSeconds())
            .playerHpRemaining(request.playerHpRemaining())
            .completedAt(Instant.now())
            .build();
        bossBattleResultRepository.save(result);

        if (request.won()) {
            medalionService.awardInternalUsage(userId, "Platinum", "Du fanget skurken!");
            userAvatarService.unlockByCondition(userId, "BOSS_DEFEAT");
        }
    }

    /**
     * Returns the most recent boss battle result for the user in the given classroom, if any.
     *
     * @return an {@link Optional} containing the latest result, or empty if none exists
     */
    public Optional<BossBattleResultResponse> getLatestResult(UUID userId, UUID classroomId) {
        Enrollment enrollment = resolveEnrollment(userId, classroomId);
        return bossBattleResultRepository
            .findTopByEnrollmentIdOrderByCompletedAtDesc(enrollment.getId())
            .map(r -> new BossBattleResultResponse(r.isWon(), r.getTimeSeconds(), r.getPlayerHpRemaining(), r.getCompletedAt()));
    }

    private Enrollment resolveEnrollment(UUID userId, UUID classroomId) {
        if (classroomId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No classroom assigned");
        }
        return enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enrolled in classroom"));
    }
}
