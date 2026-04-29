package org.ntnu.backend.medalion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.medalion.dto.AwardMedalionRequest;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.model.Medalion;
import org.ntnu.backend.medalion.repository.MedalionRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class MedalionServiceTest {
    
    @Mock
    private MedalionRepository medalionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedalionService medalionService;

    private User testUser;
    private UUID userid;

    @BeforeEach
    void setup(){
        userid = UUID.randomUUID();
        testUser = User.builder()
                    .id(userid)
                    .email("test@test.no")
                    .build();   
    }

    @Test
    void getAllUsersMedalion_returnsMappedList(){
        Medalion medalion = Medalion.builder()
                            .id(UUID.randomUUID())
                            .name("Velkommen!")
                            .description("Blabla")
                            .user(testUser)
                            .earned_at(LocalDateTime.now())
                            .build();

        when(medalionRepository.findByUserId(userid)).thenReturn(List.of(medalion));

        List<MedalionResponse> result = medalionService.getAllUsersMedalion(userid);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Velkommen!");
        assertThat(result.get(0).getImageUrl()).contains("Velkommen");
    }

    @Test                                                                                                                                                                          
    void getAllUsersMedalion_noMedalions_returnsEmptyList() {
        when(medalionRepository.findByUserId(userid)).thenReturn(List.of());                                                                                                       

        List<MedalionResponse> result = medalionService.getAllUsersMedalion(userid);                                                                                               
                
        assertThat(result).isEmpty();                                                                                                                                              
    }

    @Test
    void awardMedalion_newMedalion_savesAndReturns() {
        AwardMedalionRequest request = new AwardMedalionRequest();                                                                                                                 
        request.setUserId(userid);
        request.setName("first_login");                                                                                                                                            
        request.setDescription("Logged in for the first time");

        Medalion saved = Medalion.builder()                                                                                                                                        
                .id(UUID.randomUUID())
                .name("first_login")                                                                                                                                               
                .description("Logged in for the first time")
                .user(testUser)
                .earned_at(LocalDateTime.now())
                .build();
                                                                                                                                                                                    
        when(userRepository.findById(userid)).thenReturn(Optional.of(testUser));
        when(medalionRepository.existsByUserIdAndName(userid, "first_login")).thenReturn(false);                                                                                   
        when(medalionRepository.save(any(Medalion.class))).thenReturn(saved);
                                                                                                                                                                                    
        MedalionResponse result = medalionService.awardMedalion(request);
                                                                                                                                                                                    
        assertThat(result.getName()).isEqualTo("first_login");                                                                                                                     
        verify(medalionRepository).save(any(Medalion.class));
    }  

    @Test
    void awardMedalion_alreadyExists_doesNotSaveAgain() {
        AwardMedalionRequest request = new AwardMedalionRequest();                                                                                                                 
        request.setUserId(userid);
        request.setName("first_login");                                                                                                                                            
        request.setDescription("Logged in for the first time");

        Medalion existing = Medalion.builder()                                                                                                                                     
                .id(UUID.randomUUID())
                .name("first_login")                                                                                                                                               
                .description("Logged in for the first time")
                .user(testUser)
                .earned_at(LocalDateTime.now())
                .build();                                                                                                                                                          

        when(userRepository.findById(userid)).thenReturn(Optional.of(testUser));                                                                                                   
        when(medalionRepository.existsByUserIdAndName(userid, "first_login")).thenReturn(true);
        when(medalionRepository.findByUserIdAndName(userid, "first_login")).thenReturn(existing);
                                                                                                                                                                                    
        medalionService.awardMedalion(request);
                                                                                                                                                                                    
        verify(medalionRepository, never()).save(any());
      }

      @Test                                                                                                                                                                          
      void awardMedalion_userNotFound_throws404() {
        AwardMedalionRequest request = new AwardMedalionRequest();                                                                                                                 
        request.setUserId(userid);
        request.setName("first_login");                                                                                                                                            
        request.setDescription("Logged in for the first time");
                                                                                                                                                                                    
        when(userRepository.findById(userid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medalionService.awardMedalion(request))                                                                                                           
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("user not found");                                                                                                                           
    }

    @Test
    void getMedalCountsForUsers_whenUserIdsIsEmpty_shouldReturnEmptyMapAndNotQueryRepository() {
        Map<UUID, Integer> result = medalionService.getMedalCountsForUsers(List.of());

        assertThat(result).isEmpty();

        verify(medalionRepository, never()).countByUserIds(any());
    }

    @Test
    void getMedalCountsForUsers_shouldReturnCountsMappedByUserId() {
        UUID userOneId = UUID.randomUUID();
        UUID userTwoId = UUID.randomUUID();

        MedalionRepository.UserMedalCount countOne = mock(MedalionRepository.UserMedalCount.class);
        when(countOne.getUserId()).thenReturn(userOneId);
        when(countOne.getMedalCount()).thenReturn(3L);

        MedalionRepository.UserMedalCount countTwo = mock(MedalionRepository.UserMedalCount.class);
        when(countTwo.getUserId()).thenReturn(userTwoId);
        when(countTwo.getMedalCount()).thenReturn(1L);

        when(medalionRepository.countByUserIds(List.of(userOneId, userTwoId)))
                .thenReturn(List.of(countOne, countTwo));

        Map<UUID, Integer> result =
                medalionService.getMedalCountsForUsers(List.of(userOneId, userTwoId));

        assertThat(result).hasSize(2);
        assertThat(result.get(userOneId)).isEqualTo(3);
        assertThat(result.get(userTwoId)).isEqualTo(1);

        verify(medalionRepository).countByUserIds(List.of(userOneId, userTwoId));
    }

    @Test
    void getMedalCountsForUsers_whenDuplicateRowsForSameUser_shouldKeepFirstCount() {
        UUID userOneId = UUID.randomUUID();

        MedalionRepository.UserMedalCount firstCount = mock(MedalionRepository.UserMedalCount.class);
        when(firstCount.getUserId()).thenReturn(userOneId);
        when(firstCount.getMedalCount()).thenReturn(2L);

        MedalionRepository.UserMedalCount duplicateCount = mock(MedalionRepository.UserMedalCount.class);
        when(duplicateCount.getUserId()).thenReturn(userOneId);
        when(duplicateCount.getMedalCount()).thenReturn(5L);

        when(medalionRepository.countByUserIds(List.of(userOneId)))
                .thenReturn(List.of(firstCount, duplicateCount));

        Map<UUID, Integer> result =
                medalionService.getMedalCountsForUsers(List.of(userOneId));

        assertThat(result).hasSize(1);
        assertThat(result.get(userOneId)).isEqualTo(2);

        verify(medalionRepository).countByUserIds(List.of(userOneId));
    }

    @Test
    void awardMedalion_newMedalion_shouldSaveMedalionWithCorrectFields() {
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(userid);
        request.setName("first_login");
        request.setDescription("Logged in for the first time");

        Medalion saved = Medalion.builder()
                .id(UUID.randomUUID())
                .name("first_login")
                .description("Logged in for the first time")
                .user(testUser)
                .earned_at(LocalDateTime.of(2026, 4, 29, 12, 0))
                .build();

        when(userRepository.findById(userid)).thenReturn(Optional.of(testUser));
        when(medalionRepository.existsByUserIdAndName(userid, "first_login")).thenReturn(false);
        when(medalionRepository.save(any(Medalion.class))).thenReturn(saved);

        MedalionResponse result = medalionService.awardMedalion(request);

        assertThat(result.getName()).isEqualTo("first_login");
        assertThat(result.getDescription()).isEqualTo("Logged in for the first time");
        assertThat(result.getImageUrl()).isEqualTo("/assets/medalions/first_login.svg");

        ArgumentCaptor<Medalion> captor = ArgumentCaptor.forClass(Medalion.class);
        verify(medalionRepository).save(captor.capture());

        Medalion medalionToSave = captor.getValue();
        assertThat(medalionToSave.getName()).isEqualTo("first_login");
        assertThat(medalionToSave.getDescription()).isEqualTo("Logged in for the first time");
        assertThat(medalionToSave.getUser()).isEqualTo(testUser);
        assertThat(medalionToSave.getEarned_at()).isNotNull();
    }

    @Test
    void awardMedalion_alreadyExists_shouldReturnExistingMedalionResponse() {
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(userid);
        request.setName("first_login");
        request.setDescription("Logged in for the first time");

        UUID medalionId = UUID.randomUUID();
        LocalDateTime earnedAt = LocalDateTime.of(2026, 4, 29, 12, 0);

        Medalion existing = Medalion.builder()
                .id(medalionId)
                .name("first_login")
                .description("Already earned")
                .user(testUser)
                .earned_at(earnedAt)
                .build();

        when(userRepository.findById(userid)).thenReturn(Optional.of(testUser));
        when(medalionRepository.existsByUserIdAndName(userid, "first_login")).thenReturn(true);
        when(medalionRepository.findByUserIdAndName(userid, "first_login")).thenReturn(existing);

        MedalionResponse result = medalionService.awardMedalion(request);

        assertThat(result.getId()).isEqualTo(medalionId);
        assertThat(result.getName()).isEqualTo("first_login");
        assertThat(result.getDescription()).isEqualTo("Already earned");
        assertThat(result.getEarnedAt()).isEqualTo(earnedAt);
        assertThat(result.getImageUrl()).isEqualTo("/assets/medalions/first_login.svg");

        verify(medalionRepository, never()).save(any());
    }

    @Test
    void awardMedalion_userNotFound_shouldNotCheckExistingOrSave() {
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(userid);
        request.setName("first_login");
        request.setDescription("Logged in for the first time");

        when(userRepository.findById(userid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medalionService.awardMedalion(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("user not found");

        verify(userRepository).findById(userid);
        verify(medalionRepository, never()).existsByUserIdAndName(any(), any());
        verify(medalionRepository, never()).findByUserIdAndName(any(), any());
        verify(medalionRepository, never()).save(any());
    }

    @Test
    void awardInternalUsage_shouldBuildRequestAndDelegateToAwardMedalionFlow() {
        Medalion saved = Medalion.builder()
                .id(UUID.randomUUID())
                .name("level_completed")
                .description("Completed a level")
                .user(testUser)
                .earned_at(LocalDateTime.of(2026, 4, 29, 12, 0))
                .build();

        when(userRepository.findById(userid)).thenReturn(Optional.of(testUser));
        when(medalionRepository.existsByUserIdAndName(userid, "level_completed")).thenReturn(false);
        when(medalionRepository.save(any(Medalion.class))).thenReturn(saved);

        medalionService.awardInternalUsage(userid, "level_completed", "Completed a level");

        ArgumentCaptor<Medalion> captor = ArgumentCaptor.forClass(Medalion.class);
        verify(medalionRepository).save(captor.capture());

        Medalion medalionToSave = captor.getValue();
        assertThat(medalionToSave.getName()).isEqualTo("level_completed");
        assertThat(medalionToSave.getDescription()).isEqualTo("Completed a level");
        assertThat(medalionToSave.getUser()).isEqualTo(testUser);
    }
}
