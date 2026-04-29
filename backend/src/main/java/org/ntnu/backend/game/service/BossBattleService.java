package org.ntnu.backend.game.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.dto.BossBattleResultResponse;
import org.ntnu.backend.game.dto.BossBattleSaveRequest;
import org.ntnu.backend.game.model.BossBattleResult;
import org.ntnu.backend.game.repository.BossBattleResultRepository;
import org.ntnu.backend.medalion.service.MedalionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BossBattleService {

    private final BossBattleResultRepository bossBattleResultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MedalionService medalionService;
    private final UserAvatarService userAvatarService;

    public void saveResult(UUID userId, UUID classroomId, BossBattleSaveRequest request) {
        if(request.won()){
            medalionService.awardInternalUsage(userId, "Platinum", "Du fanget skurken!");
            userAvatarService.unlockByCondition(userId, "BOSS_DEFEAT");
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
    }

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
