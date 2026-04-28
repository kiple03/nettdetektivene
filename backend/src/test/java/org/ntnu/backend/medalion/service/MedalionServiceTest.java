package org.ntnu.backend.medalion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
}
