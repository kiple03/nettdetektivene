//package org.ntnu.backend.classroom.controller;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//
//import java.util.List;
//import java.util.UUID;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
//import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
//import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
//import org.ntnu.backend.classroom.service.LeaderboardService;
//import org.ntnu.backend.game.service.ClassroomAuthorizationService;
//import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
//import org.springframework.security.access.AccessDeniedException;
//
//class LeaderboardControllerTest {
//
//    private LeaderboardService leaderboardService;
//    private ClassroomAuthorizationService classroomAuthorizationService;
//    private LeaderboardController leaderboardController;
//    private JwtAuthenticatedPrincipal principal;
//
//    private UUID classroomId;
//    private UUID teacherId;
//
//    @BeforeEach
//    void setUp() {
//        leaderboardService = mock(LeaderboardService.class);
//        classroomAuthorizationService = mock(ClassroomAuthorizationService.class);
//        principal = mock(JwtAuthenticatedPrincipal.class);
//
//        leaderboardController = new LeaderboardController(
//                leaderboardService,
//                classroomAuthorizationService
//        );
//
//        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
//        teacherId = UUID.fromString("22222222-2222-2222-2222-222222222222");
//    }
//
//    @Test
//    void getGlobalLeaderboard_shouldReturnGlobalLeaderboardFromService() {
//        RankedClassroomResponse first = mock(RankedClassroomResponse.class);
//        RankedClassroomResponse second = mock(RankedClassroomResponse.class);
//
//        when(leaderboardService.getGlobalLeaderboard())
//                .thenReturn(List.of(first, second));
//
//        List<RankedClassroomResponse> result =
//                leaderboardController.getGlobalLeaderboard();
//
//        assertEquals(List.of(first, second), result);
//
//        verify(leaderboardService).getGlobalLeaderboard();
//        verifyNoInteractions(classroomAuthorizationService);
//    }
//
//    @Test
//    void getGlobalLeaderboard_whenServiceReturnsEmptyList_shouldReturnEmptyList() {
//        when(leaderboardService.getGlobalLeaderboard())
//                .thenReturn(List.of());
//
//        List<RankedClassroomResponse> result =
//                leaderboardController.getGlobalLeaderboard();
//
//        assertEquals(List.of(), result);
//
//        verify(leaderboardService).getGlobalLeaderboard();
//        verifyNoInteractions(classroomAuthorizationService);
//    }
//
//    @Test
//    void getClassroomLeaderboard_shouldVerifyAccessAndReturnRankedStudents() {
//        RankedStudentResponse first = mock(RankedStudentResponse.class);
//        RankedStudentResponse second = mock(RankedStudentResponse.class);
//
//        when(leaderboardService.getRankedStudentLeaderboard(classroomId))
//                .thenReturn(List.of(first, second));
//
//        List<RankedStudentResponse> result =
//                leaderboardController.getClassroomLeaderboard(classroomId, principal);
//
//        assertEquals(List.of(first, second), result);
//
//        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
//        verify(leaderboardService).getRankedStudentLeaderboard(classroomId);
//    }
//
//    @Test
//    void getClassroomLeaderboard_whenAccessDenied_shouldThrowAndNotCallService() {
//        doThrow(new AccessDeniedException("No access"))
//                .when(classroomAuthorizationService)
//                .verifyAccess(principal, classroomId);
//
//        AccessDeniedException exception = org.junit.jupiter.api.Assertions.assertThrows(
//                AccessDeniedException.class,
//                () -> leaderboardController.getClassroomLeaderboard(classroomId, principal)
//        );
//
//        assertEquals("No access", exception.getMessage());
//
//        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
//        verify(leaderboardService, never()).getRankedStudentLeaderboard(any());
//    }
//
//    @Test
//    void getClassroomLeaderboard_whenServiceReturnsEmptyList_shouldReturnEmptyList() {
//        when(leaderboardService.getRankedStudentLeaderboard(classroomId))
//                .thenReturn(List.of());
//
//        List<RankedStudentResponse> result =
//                leaderboardController.getClassroomLeaderboard(classroomId, principal);
//
//        assertEquals(List.of(), result);
//
//        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
//        verify(leaderboardService).getRankedStudentLeaderboard(classroomId);
//    }
//
//    @Test
//    void getTeacherClassroomScores_shouldReturnScoresFromService() {
//        LeaderboardClassroomResponse first =
//                new LeaderboardClassroomResponse(
//                        classroomId.toString(),
//                        "Klasse 5A",
//                        900,
//                        450
//                );
//
//        LeaderboardClassroomResponse second =
//                new LeaderboardClassroomResponse(
//                        UUID.fromString("33333333-3333-3333-3333-333333333333").toString(),
//                        "Klasse 6B",
//                        600,
//                        300
//                );
//
//        when(leaderboardService.getTeacherClassroomScores(teacherId))
//                .thenReturn(List.of(first, second));
//
//        List<LeaderboardClassroomResponse> result =
//                leaderboardController.getTeacherClassroomScores(teacherId, principal);
//
//        assertEquals(List.of(first, second), result);
//
//        verify(leaderboardService).getTeacherClassroomScores(teacherId);
//        verifyNoInteractions(classroomAuthorizationService);
//    }
//
//    @Test
//    void getTeacherClassroomScores_whenServiceReturnsEmptyList_shouldReturnEmptyList() {
//        when(leaderboardService.getTeacherClassroomScores(teacherId))
//                .thenReturn(List.of());
//
//        List<LeaderboardClassroomResponse> result =
//                leaderboardController.getTeacherClassroomScores(teacherId, principal);
//
//        assertEquals(List.of(), result);
//
//        verify(leaderboardService).getTeacherClassroomScores(teacherId);
//        verifyNoInteractions(classroomAuthorizationService);
//    }
//}