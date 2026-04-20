package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

  @Mock private StopRepository stopRepository;
  @Mock private LevelRepository levelRepository;

  private GameService gameService;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    OptionValidator optionValidator = new OptionValidator(
        Validation.buildDefaultValidatorFactory().getValidator());
    OptionParser optionParser = new OptionParser(new ObjectMapper(), optionValidator);
    GameMapper gameMapper = new GameMapper(optionParser);
    gameService = new GameService(stopRepository, levelRepository, gameMapper);
  }

  // ===== getStopsForUser =====

  @Test
  void getStopsForUser_returnsAllStopsOrdered() {
    Stop stop1 = new Stop();
    stop1.setId(1);
    stop1.setName("Nyhetskvartalet");
    stop1.setDisplayOrder(1);

    Stop stop2 = new Stop();
    stop2.setId(2);
    stop2.setName("Fotografen");
    stop2.setDisplayOrder(2);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(stop1, stop2));

    List<StopResponse> result = gameService.getStopsForUser(userId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).name()).isEqualTo("Nyhetskvartalet");
    assertThat(result.get(1).name()).isEqualTo("Fotografen");
  }

  @Test
  void getStopsForUser_allUnlocked() {
    Stop stop = new Stop();
    stop.setId(1);
    stop.setName("Test");
    stop.setDisplayOrder(1);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(stop));

    List<StopResponse> result = gameService.getStopsForUser(userId);

    assertThat(result.get(0).locked()).isFalse();
  }

  @Test
  void getStopsForUser_emptyDb_returnsEmptyList() {
    when(stopRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of());

    List<StopResponse> result = gameService.getStopsForUser(userId);

    assertThat(result).isEmpty();
  }

  // ===== getLevel =====

  @Test
  void getLevel_found_returnsResponse() {
    Stop stop = new Stop();
    stop.setId(1);

    Level level = new Level();
    level.setId(1);
    level.setTitle("Ekte eller falskt?");
    level.setDescription("Finn den ekte nyheten");
    level.setLevelType(LevelType.MULTIPLE_CHOICE);
    level.setStop(stop);

    when(levelRepository.findById(1)).thenReturn(Optional.of(level));

    LevelResponse result = gameService.getLevel(1, userId);

    assertThat(result.id()).isEqualTo(1);
    assertThat(result.title()).isEqualTo("Ekte eller falskt?");
    assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    assertThat(result.locked()).isFalse();
  }

  @Test
  void getLevel_notFound_throws() {
    when(levelRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gameService.getLevel(99, userId))
        .isInstanceOf(LevelNotFoundException.class);
  }

  // ===== getLevelsForStop =====

  @Test
  void getLevelsForStop_returnsLevelsForStop() {
    Stop stop = new Stop();
    stop.setId(1);

    Level level1 = new Level();
    level1.setId(1);
    level1.setTitle("Level 1");
    level1.setDescription("First");
    level1.setLevelType(LevelType.MULTIPLE_CHOICE);
    level1.setStop(stop);

    Level level2 = new Level();
    level2.setId(2);
    level2.setTitle("Level 2");
    level2.setDescription("Second");
    level2.setLevelType(LevelType.MULTIPLE_CHOICE);
    level2.setStop(stop);

    when(levelRepository.findByStopIdOrderByIdAsc(1))
        .thenReturn(List.of(level1, level2));

    List<LevelResponse> result = gameService.getLevelsForStop(1, userId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo("Level 1");
    assertThat(result.get(1).title()).isEqualTo("Level 2");
  }

  @Test
  void getLevelsForStop_noLevels_returnsEmptyList() {
    when(levelRepository.findByStopIdOrderByIdAsc(99))
        .thenReturn(List.of());

    List<LevelResponse> result = gameService.getLevelsForStop(99, userId);

    assertThat(result).isEmpty();
  }
}