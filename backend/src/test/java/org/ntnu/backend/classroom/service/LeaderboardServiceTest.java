package org.ntnu.backend.classroom.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardStudentResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
import org.ntnu.backend.classroom.mapper.LeaderboardMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.user.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class LeaderboardServiceTest {

    private EnrollmentRepository enrollmentRepository;
    private UserProgressRepository userProgressRepository;
    private StopRepository stopRepository;
    private LeaderboardMapper leaderboardMapper;
    private ClassroomTeacherRepository classroomTeacherRepository;

    private LeaderboardService leaderboardService;

    private UUID classroomId;
    private UUID teacherId;
    private UUID studentOneId;
    private UUID studentTwoId;
    private UUID studentThreeId;

    private Classroom classroomOne;
    private Classroom classroomTwo;

    private User studentOne;
    private User studentTwo;
    private User studentThree;

    private Enrollment enrollmentOne;
    private Enrollment enrollmentTwo;
    private Enrollment enrollmentThree;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        userProgressRepository = mock(UserProgressRepository.class);
        stopRepository = mock(StopRepository.class);
        leaderboardMapper = mock(LeaderboardMapper.class);
        classroomTeacherRepository = mock(ClassroomTeacherRepository.class);

        leaderboardService = new LeaderboardService(
                enrollmentRepository,
                userProgressRepository,
                stopRepository,
                leaderboardMapper,
                classroomTeacherRepository
        );

        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        teacherId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        studentOneId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        studentTwoId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        studentThreeId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        classroomOne = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .joinCode("ABC123")
                .build();

        classroomTwo = Classroom.builder()
                .id(UUID.fromString("66666666-6666-6666-6666-666666666666"))
                .title("Klasse 6B")
                .joinCode("DEF456")
                .build();

        studentOne = User.builder()
                .id(studentOneId)
                .username("Anna")
                .email("anna@example.com")
                .build();

        studentTwo = User.builder()
                .id(studentTwoId)
                .username("Birk")
                .email("birk@example.com")
                .build();

        studentThree = User.builder()
                .id(studentThreeId)
                .username("Clara")
                .email("clara@example.com")
                .build();

        enrollmentOne = Enrollment.builder()
                .user(studentOne)
                .classroom(classroomOne)
                .build();

        enrollmentTwo = Enrollment.builder()
                .user(studentTwo)
                .classroom(classroomOne)
                .build();

        enrollmentThree = Enrollment.builder()
                .user(studentThree)
                .classroom(classroomTwo)
                .build();
    }

    @Test
    void computeScores_shouldCalculateScoresFromUserProgress() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);
        StopLevelCount stopTwo = mockStopLevelCount(2, 2L);
        StopLevelCount stopThree = mockStopLevelCount(3, 4L);

        Stop currentStopForStudentOne = mockStop(2);
        Stop currentStopForStudentTwo = mockStop(3);

        UserProgress progressOne = mock(UserProgress.class);
        when(progressOne.getId()).thenReturn(studentOneId);
        when(progressOne.isGameCompleted()).thenReturn(false);
        when(progressOne.getCurrentStop()).thenReturn(currentStopForStudentOne);
        when(progressOne.getCurrentLevelOrder()).thenReturn(2);

        UserProgress progressTwo = mock(UserProgress.class);
        when(progressTwo.getId()).thenReturn(studentTwoId);
        when(progressTwo.isGameCompleted()).thenReturn(false);
        when(progressTwo.getCurrentStop()).thenReturn(currentStopForStudentTwo);
        when(progressTwo.getCurrentLevelOrder()).thenReturn(1);

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne, stopTwo, stopThree));

        when(userProgressRepository.findAllById(List.of(studentOneId, studentTwoId)))
                .thenReturn(List.of(progressOne, progressTwo));

        Map<UUID, Integer> result = leaderboardService.computeScores(List.of(enrollmentOne, enrollmentTwo));

        assertEquals(400, result.get(studentOneId));
        assertEquals(500, result.get(studentTwoId));

        verify(stopRepository).findAllWithLevelCounts();
        verify(userProgressRepository).findAllById(List.of(studentOneId, studentTwoId));
    }

    @Test
    void computeScores_whenGameCompleted_shouldReturnFullScore() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);
        StopLevelCount stopTwo = mockStopLevelCount(2, 2L);
        StopLevelCount stopThree = mockStopLevelCount(3, 4L);

        UserProgress completedProgress = mock(UserProgress.class);
        when(completedProgress.getId()).thenReturn(studentOneId);
        when(completedProgress.isGameCompleted()).thenReturn(true);

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne, stopTwo, stopThree));

        when(userProgressRepository.findAllById(List.of(studentOneId)))
                .thenReturn(List.of(completedProgress));

        Map<UUID, Integer> result = leaderboardService.computeScores(List.of(enrollmentOne));

        assertEquals(900, result.get(studentOneId));

        verify(stopRepository).findAllWithLevelCounts();
        verify(userProgressRepository).findAllById(List.of(studentOneId));
    }

    @Test
    void getRankedStudentLeaderboard_shouldSortStudentsByScoreAndUseMapper() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);
        StopLevelCount stopTwo = mockStopLevelCount(2, 2L);

        Stop stopTwoModel = mockStop(2);

        UserProgress progressOne = mock(UserProgress.class);
        when(progressOne.getId()).thenReturn(studentOneId);
        when(progressOne.isGameCompleted()).thenReturn(false);
        when(progressOne.getCurrentStop()).thenReturn(stopTwoModel);
        when(progressOne.getCurrentLevelOrder()).thenReturn(1);

        UserProgress progressTwo = mock(UserProgress.class);
        when(progressTwo.getId()).thenReturn(studentTwoId);
        when(progressTwo.isGameCompleted()).thenReturn(true);

        RankedStudentResponse rankedOne = mock(RankedStudentResponse.class);
        RankedStudentResponse rankedTwo = mock(RankedStudentResponse.class);

        when(enrollmentRepository.findAllByClassroomIdAndLeftAtIsNull(classroomId))
                .thenReturn(List.of(enrollmentOne, enrollmentTwo));

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne, stopTwo));

        when(userProgressRepository.findAllById(List.of(studentOneId, studentTwoId)))
                .thenReturn(List.of(progressOne, progressTwo));

        when(leaderboardMapper.toRankedStudentList(anyList()))
                .thenReturn(List.of(rankedOne, rankedTwo));

        List<RankedStudentResponse> result =
                leaderboardService.getRankedStudentLeaderboard(classroomId);

        assertEquals(List.of(rankedOne, rankedTwo), result);

        verify(enrollmentRepository).findAllByClassroomIdAndLeftAtIsNull(classroomId);

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(leaderboardMapper).toRankedStudentList(captor.capture());

        List<LeaderboardStudentResponse> sortedStudents =
                (List<LeaderboardStudentResponse>) captor.getValue();

        assertEquals(2, sortedStudents.size());
        assertEquals("Birk", sortedStudents.get(0).username());
        assertEquals(500, sortedStudents.get(0).score());
        assertEquals("Anna", sortedStudents.get(1).username());
        assertEquals(300, sortedStudents.get(1).score());
    }

    @Test
    void getRankedStudentLeaderboard_whenStudentHasNoProgress_shouldUseZeroScore() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);

        when(enrollmentRepository.findAllByClassroomIdAndLeftAtIsNull(classroomId))
                .thenReturn(List.of(enrollmentOne));

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne));

        when(userProgressRepository.findAllById(List.of(studentOneId)))
                .thenReturn(List.of());

        when(leaderboardMapper.toRankedStudentList(anyList()))
                .thenReturn(List.of());

        leaderboardService.getRankedStudentLeaderboard(classroomId);

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(leaderboardMapper).toRankedStudentList(captor.capture());

        List<LeaderboardStudentResponse> students =
                (List<LeaderboardStudentResponse>) captor.getValue();

        assertEquals(1, students.size());
        assertEquals("Anna", students.get(0).username());
        assertEquals(0, students.get(0).score());
    }

    @Test
    void getTeacherClassroomScores_shouldReturnSortedClassroomScoresForTeacher() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);
        StopLevelCount stopTwo = mockStopLevelCount(2, 2L);

        UserProgress progressOne = completedProgress(studentOneId);
        UserProgress progressTwo = progressAt(studentTwoId, 2, 1);
        UserProgress progressThree = progressAt(studentThreeId, 2, 2);

        when(classroomTeacherRepository.findClassroomsByTeacherId(teacherId))
                .thenReturn(List.of(classroomOne, classroomTwo));

        when(enrollmentRepository.findAllByClassroomIdInAndLeftAtIsNull(
                List.of(classroomOne.getId(), classroomTwo.getId())))
                .thenReturn(List.of(enrollmentOne, enrollmentTwo, enrollmentThree));

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne, stopTwo));

        when(userProgressRepository.findAllById(List.of(studentOneId, studentTwoId, studentThreeId)))
                .thenReturn(List.of(progressOne, progressTwo, progressThree));

        List<LeaderboardClassroomResponse> result =
                leaderboardService.getTeacherClassroomScores(teacherId);

        assertEquals(2, result.size());

        Map<String, LeaderboardClassroomResponse> byId = result.stream()
                .collect(java.util.stream.Collectors.toMap(
                        LeaderboardClassroomResponse::id,
                        response -> response
                ));

        LeaderboardClassroomResponse klasse5A = byId.get(classroomOne.getId().toString());
        LeaderboardClassroomResponse klasse6B = byId.get(classroomTwo.getId().toString());

        assertNotNull(klasse5A);
        assertEquals("Klasse 5A", klasse5A.name());
        assertEquals(800, klasse5A.totalScore());
        assertEquals(400, klasse5A.avgScore());

        assertNotNull(klasse6B);
        assertEquals("Klasse 6B", klasse6B.name());
        assertEquals(400, klasse6B.totalScore());
        assertEquals(400, klasse6B.avgScore());

        verify(classroomTeacherRepository).findClassroomsByTeacherId(teacherId);
        verify(enrollmentRepository).findAllByClassroomIdInAndLeftAtIsNull(
                List.of(classroomOne.getId(), classroomTwo.getId()));
    }

    @Test
    void getGlobalLeaderboard_shouldBuildSortedLeaderboardAndUseMapper() {
        StopLevelCount stopOne = mockStopLevelCount(1, 3L);
        StopLevelCount stopTwo = mockStopLevelCount(2, 2L);

        UserProgress progressOne = completedProgress(studentOneId);
        UserProgress progressTwo = progressAt(studentTwoId, 2, 1);
        UserProgress progressThree = progressAt(studentThreeId, 2, 2);

        RankedClassroomResponse rankedOne = mock(RankedClassroomResponse.class);
        RankedClassroomResponse rankedTwo = mock(RankedClassroomResponse.class);

        when(enrollmentRepository.findAllByLeftAtIsNull())
                .thenReturn(List.of(enrollmentOne, enrollmentTwo, enrollmentThree));

        when(stopRepository.findAllWithLevelCounts())
                .thenReturn(List.of(stopOne, stopTwo));

        when(userProgressRepository.findAllById(List.of(studentOneId, studentTwoId, studentThreeId)))
                .thenReturn(List.of(progressOne, progressTwo, progressThree));

        when(leaderboardMapper.toRankedList(anyList()))
                .thenReturn(List.of(rankedOne, rankedTwo));

        List<RankedClassroomResponse> result = leaderboardService.getGlobalLeaderboard();

        assertEquals(List.of(rankedOne, rankedTwo), result);

        verify(enrollmentRepository).findAllByLeftAtIsNull();

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(leaderboardMapper).toRankedList(captor.capture());

        List<LeaderboardClassroomResponse> classrooms =
                (List<LeaderboardClassroomResponse>) captor.getValue();

        Map<String, LeaderboardClassroomResponse> byId = classrooms.stream()
                .collect(java.util.stream.Collectors.toMap(
                        LeaderboardClassroomResponse::id,
                        response -> response
                ));

        LeaderboardClassroomResponse klasse5A = byId.get(classroomOne.getId().toString());
        LeaderboardClassroomResponse klasse6B = byId.get(classroomTwo.getId().toString());

        assertNotNull(klasse5A);
        assertEquals("Klasse 5A", klasse5A.name());
        assertEquals(800, klasse5A.totalScore());
        assertEquals(400, klasse5A.avgScore());

        assertNotNull(klasse6B);
        assertEquals("Klasse 6B", klasse6B.name());
        assertEquals(400, klasse6B.totalScore());
        assertEquals(400, klasse6B.avgScore());
    }

    private StopLevelCount mockStopLevelCount(int displayOrder, long levelCount) {
        StopLevelCount count = mock(StopLevelCount.class);

        doReturn(displayOrder).when(count).getDisplayOrder();
        doReturn(levelCount).when(count).getLevelCount();

        return count;
    }

    private Stop mockStop(int displayOrder) {
        Stop stop = mock(Stop.class);
        when(stop.getDisplayOrder()).thenReturn(displayOrder);
        return stop;
    }

    private UserProgress completedProgress(UUID userId) {
        UserProgress progress = mock(UserProgress.class);
        when(progress.getId()).thenReturn(userId);
        when(progress.isGameCompleted()).thenReturn(true);
        return progress;
    }

    private UserProgress progressAt(UUID userId, int stopDisplayOrder, int currentLevelOrder) {
        Stop stop = mockStop(stopDisplayOrder);

        UserProgress progress = mock(UserProgress.class);
        when(progress.getId()).thenReturn(userId);
        when(progress.isGameCompleted()).thenReturn(false);
        when(progress.getCurrentStop()).thenReturn(stop);
        when(progress.getCurrentLevelOrder()).thenReturn(currentLevelOrder);

        return progress;
    }
}