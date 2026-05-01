package org.ntnu.backend.classroom.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardStudentResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;

class LeaderboardMapperTest {

    private LeaderboardMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LeaderboardMapper();
    }

    @Test
    void toRankedList_shouldAssignRanksStartingAtOneAndKeepOrder() {
        LeaderboardClassroomResponse classroomOne = new LeaderboardClassroomResponse(
                "11111111-1111-1111-1111-111111111111",
                "Klasse 5A",
                900,
                450
        );

        LeaderboardClassroomResponse classroomTwo = new LeaderboardClassroomResponse(
                "22222222-2222-2222-2222-222222222222",
                "Klasse 6B",
                700,
                350
        );

        LeaderboardClassroomResponse classroomThree = new LeaderboardClassroomResponse(
                "33333333-3333-3333-3333-333333333333",
                "Klasse 7C",
                300,
                150
        );

        List<RankedClassroomResponse> result =
                mapper.toRankedList(List.of(classroomOne, classroomTwo, classroomThree));

        assertThat(result).hasSize(3);

        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).classroom()).isSameAs(classroomOne);
        assertThat(result.get(0).classroom().name()).isEqualTo("Klasse 5A");

        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).classroom()).isSameAs(classroomTwo);
        assertThat(result.get(1).classroom().name()).isEqualTo("Klasse 6B");

        assertThat(result.get(2).rank()).isEqualTo(3);
        assertThat(result.get(2).classroom()).isSameAs(classroomThree);
        assertThat(result.get(2).classroom().name()).isEqualTo("Klasse 7C");
    }

    @Test
    void toRankedList_whenInputHasOneClassroom_shouldAssignRankOne() {
        LeaderboardClassroomResponse classroom = new LeaderboardClassroomResponse(
                "11111111-1111-1111-1111-111111111111",
                "Klasse 5A",
                900,
                450
        );

        List<RankedClassroomResponse> result = mapper.toRankedList(List.of(classroom));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).classroom()).isSameAs(classroom);
    }

    @Test
    void toRankedList_whenInputIsEmpty_shouldReturnEmptyList() {
        List<RankedClassroomResponse> result = mapper.toRankedList(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void toRankedStudentList_shouldAssignRanksStartingAtOneAndKeepOrder() {
        LeaderboardStudentResponse studentOne = new LeaderboardStudentResponse(
                "Anna",
                900
        );

        LeaderboardStudentResponse studentTwo = new LeaderboardStudentResponse(
                "Birk",
                700
        );

        LeaderboardStudentResponse studentThree = new LeaderboardStudentResponse(
                "Clara",
                300
        );

        List<RankedStudentResponse> result =
                mapper.toRankedStudentList(List.of(studentOne, studentTwo, studentThree));

        assertThat(result).hasSize(3);

        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).student()).isSameAs(studentOne);
        assertThat(result.get(0).student().username()).isEqualTo("Anna");

        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).student()).isSameAs(studentTwo);
        assertThat(result.get(1).student().username()).isEqualTo("Birk");

        assertThat(result.get(2).rank()).isEqualTo(3);
        assertThat(result.get(2).student()).isSameAs(studentThree);
        assertThat(result.get(2).student().username()).isEqualTo("Clara");
    }

    @Test
    void toRankedStudentList_whenInputHasOneStudent_shouldAssignRankOne() {
        LeaderboardStudentResponse student = new LeaderboardStudentResponse(
                "Anna",
                900
        );

        List<RankedStudentResponse> result = mapper.toRankedStudentList(List.of(student));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).student()).isSameAs(student);
    }

    @Test
    void toRankedStudentList_whenInputIsEmpty_shouldReturnEmptyList() {
        List<RankedStudentResponse> result = mapper.toRankedStudentList(List.of());

        assertThat(result).isEmpty();
    }
}