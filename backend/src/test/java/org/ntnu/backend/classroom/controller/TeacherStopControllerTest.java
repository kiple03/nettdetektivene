package org.ntnu.backend.classroom.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.service.TeacherStopService;

class TeacherStopControllerTest {

    private TeacherStopService teacherStopService;
    private TeacherStopController teacherStopController;

    @BeforeEach
    void setUp() {
        teacherStopService = mock(TeacherStopService.class);
        teacherStopController = new TeacherStopController(teacherStopService);
    }

    @Test
    void listStops_shouldReturnStopsFromService() {
        StopResponse stopOne = StopResponse.builder()
                .id(1)
                .name("Start")
                .displayOrder(1)
                .build();

        StopResponse stopTwo = StopResponse.builder()
                .id(2)
                .name("Passord")
                .displayOrder(2)
                .build();

        when(teacherStopService.listStops())
                .thenReturn(List.of(stopOne, stopTwo));

        List<StopResponse> result = teacherStopController.listStops();

        assertEquals(2, result.size());
        assertEquals(stopOne, result.get(0));
        assertEquals(stopTwo, result.get(1));

        verify(teacherStopService).listStops();
    }

    @Test
    void listStops_whenNoStopsExist_shouldReturnEmptyList() {
        when(teacherStopService.listStops())
                .thenReturn(List.of());

        List<StopResponse> result = teacherStopController.listStops();

        assertEquals(List.of(), result);

        verify(teacherStopService).listStops();
    }
}