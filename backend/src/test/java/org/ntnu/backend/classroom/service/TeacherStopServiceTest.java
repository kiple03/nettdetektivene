package org.ntnu.backend.classroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.repository.StopRepository;

class TeacherStopServiceTest {

    private StopRepository stopRepository;
    private TeacherStopService teacherStopService;

    @BeforeEach
    void setUp() {
        stopRepository = mock(StopRepository.class);
        teacherStopService = new TeacherStopService(stopRepository);
    }

    @Test
    void listStops_shouldReturnStopsMappedToResponses() {
        Stop stopOne = stop(1, "Start", 1);
        Stop stopTwo = stop(2, "Passord", 2);

        when(stopRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(stopOne, stopTwo));

        List<StopResponse> result = teacherStopService.listStops();

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getId());
        assertEquals("Start", result.get(0).getName());
        assertEquals(1, result.get(0).getDisplayOrder());

        assertEquals(2, result.get(1).getId());
        assertEquals("Passord", result.get(1).getName());
        assertEquals(2, result.get(1).getDisplayOrder());

        verify(stopRepository).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void listStops_whenNoStopsExist_shouldReturnEmptyList() {
        when(stopRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of());

        List<StopResponse> result = teacherStopService.listStops();

        assertTrue(result.isEmpty());

        verify(stopRepository).findAllByOrderByDisplayOrderAsc();
    }

    private Stop stop(int id, String name, int displayOrder) {
        Stop stop = new Stop();
        stop.setId(id);
        stop.setName(name);
        stop.setDisplayOrder(displayOrder);
        return stop;
    }
}