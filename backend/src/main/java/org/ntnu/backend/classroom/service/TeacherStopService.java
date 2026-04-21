package org.ntnu.backend.classroom.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.game.repository.StopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides the teacher's read-only view of the game map's stops.
 * Kept separate from the student game endpoints so teacher-only
 * concerns (e.g. future editing of stops/questions) can be added
 * without affecting the student flow.
 */
@Service
@RequiredArgsConstructor
public class TeacherStopService {

    private final StopRepository stopRepository;

    @Transactional(readOnly = true)
    public List<StopResponse> listStops() {
        return stopRepository.findAllByOrderByDisplayOrderAsc()
            .stream()
            .map(stop -> StopResponse.builder()
                .id(stop.getId())
                .name(stop.getName())
                .displayOrder(stop.getDisplayOrder())
                .build())
            .toList();
    }
}
