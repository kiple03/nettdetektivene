package org.ntnu.backend.medalion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.ntnu.backend.medalion.dto.AwardMedalionRequest;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.model.Medalion;
import org.ntnu.backend.medalion.repository.MedalionRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MedalionService {
    private final MedalionRepository medalionRepository;
    private final UserRepository userRepository;

    public List<MedalionResponse> getAllUsersMedalion(UUID user){
        return medalionRepository.findByUserId(user).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MedalionResponse awardMedalion(AwardMedalionRequest request){
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Medalion medalion = Medalion.builder()
            .name(request.getName())
            .description(request.getDescription())
            .user_id(user)
            .earned_at(LocalDateTime.now())
            .build();

        return toResponse(medalionRepository.save(medalion));
    }

    private MedalionResponse toResponse(Medalion medalion){
        return MedalionResponse.builder()
            .id(medalion.getId())
            .name(medalion.getName())
            .description(medalion.getDescription())
            .earnedAt(medalion.getEarned_at())
            .build();
    }

}
