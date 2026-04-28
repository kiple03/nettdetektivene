package org.ntnu.backend.classroom.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.repository.StopRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherStopServiceTest {

    private StopRepository stopRepository;
    private TeacherStopService teacherStopService;

    private int stopOneId;
    private int stopTwoId;

    @BeforeEach
    void setUp() {
        stopRepository = mock(StopRepository.class);
        teacherStopService = new TeacherStopService(stopRepository);

        stopOneId = 1;
        stopTwoId = 2;
    }

    @Test
    void listStops_shouldReturnStopsMappedToResponses() {
        Stop stopOne = mockStop(stopOneId, "Start", 1);
        Stop stopTwo = mockStop(stopTwoId, "Passord", 2);

        when(stopRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(stopOne, stopTwo));

        List<StopResponse> result = teacherStopService.listStops();

        assertEquals(2, result.size());

        assertEquals(stopOneId, result.get(0).getId());
        assertEquals("Start", result.get(0).getName());
        assertEquals(1, result.get(0).getDisplayOrder());

        assertEquals(stopTwoId, result.get(1).getId());
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

    private Stop mockStop(int id, String name, int displayOrder) {
        Stop stop = mock(Stop.class);

        when(stop.getId()).thenReturn(id);
        when(stop.getName()).thenReturn(name);
        when(stop.getDisplayOrder()).thenReturn(displayOrder);

        return stop;
    }
}