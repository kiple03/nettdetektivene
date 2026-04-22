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


/**
 * Service class for medalion logic.
 * Handles awarding and fetching medalions for users.
 * Awarding is idempotent — a user will never receive the same medalion twice.
 */
@Service
@RequiredArgsConstructor
public class MedalionService {
    private final MedalionRepository medalionRepository;
    private final UserRepository userRepository;

    /**
     * Returns all medalions earned by a specific user.
     *
     * @param user the ID of the user
     * @return a list of {@link MedalionResponse} belonging to the user
     */
    public List<MedalionResponse> getAllUsersMedalion(UUID user){
        return medalionRepository.findByUserId(user).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Awards a medalion to a user based on the given request.
     * If the user already has a medalion with the same name, the existing one is returned without saving a duplicate.
     *
     * @param request the request containing userId, name, and description
     * @return the awarded or already existing {@link MedalionResponse}
     * @throws ResponseStatusException with 404 if the user is not found
     */
    @Transactional
    public MedalionResponse awardMedalion(AwardMedalionRequest request){
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if(medalionRepository.existsByUserIdAndName(request.getUserId(), request.getName())){
            return toResponse(medalionRepository.findByUserIdAndName(request.getUserId(), request.getName()));
        }

        Medalion medalion = Medalion.builder()
            .name(request.getName())
            .description(request.getDescription())
            .user(user)
            .earned_at(LocalDateTime.now())
            .build();

        return toResponse(medalionRepository.save(medalion));
    }

    /**
     * Convenience method for awarding a medalion internally from other services.
     * Builds the request and delegates to {@link #awardMedalion(AwardMedalionRequest)}.
     *
     * @param userId the ID of the user to award
     * @param name the name of the medalion
     * @param description the description of the achievement
     */
    public void awardInternalUsage(UUID userId, String name, String description){
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setDescription(description);
        awardMedalion(request);
    }

    /**
     * Maps a {@link Medalion} entity to a {@link MedalionResponse} DTO.
     * Constructs the image URL from the medalion name.
     *
     * @param medalion the entity to map
     * @return the mapped response DTO
     */
    private MedalionResponse toResponse(Medalion medalion){
        return MedalionResponse.builder()
            .id(medalion.getId())
            .name(medalion.getName())
            .description(medalion.getDescription())
            .earnedAt(medalion.getEarned_at())
            .imageUrl("/assets/medalions/" + medalion.getName() + ".svg")
            .build();
    }

}
