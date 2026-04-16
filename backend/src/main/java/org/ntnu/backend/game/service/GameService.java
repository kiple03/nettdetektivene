package org.ntnu.backend.game.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {

  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final GameMapper gameMapper;


  // needs to set lockead/unlocked later
  // hgardcoded all unlocked
  public List<StopResponse> getStopsForUser(UUID userId) {
    return stopRepository.findAll()
        .stream()
        .map(stop -> gameMapper.toStopOverviewResponse(stop, false))
        .toList();
  }

  public LevelResponse getLevel(int levelId, UUID userId) {
    return levelRepository.findById(levelId)
        .map(level -> gameMapper.toLevelResponse(level, false))
        .orElseThrow(() -> new LevelNotFoundException(levelId));
  }
}
