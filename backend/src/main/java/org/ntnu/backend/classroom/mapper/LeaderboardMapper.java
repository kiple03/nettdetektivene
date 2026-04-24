package org.ntnu.backend.classroom.mapper;

import java.util.List;
import java.util.stream.IntStream;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardMapper {

  public List<RankedClassroomResponse> toRankedList(List<LeaderboardClassroomResponse> sorted) {
    return IntStream.range(0, sorted.size())
        .mapToObj(i -> new RankedClassroomResponse(i + 1, sorted.get(i)))
        .toList();
  }
}
