package org.ntnu.backend.classroom.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class TeacherClassroomControllerTest {

    @Mock
    private TeacherClassroomService teacherClassroomService;

    @InjectMocks
    private TeacherClassroomController teacherClassroomController;

    @Test
    void listClassrooms_delegatesToService() {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", teacherId, "TEACHER", null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );

        ClassroomSummaryResponse summary = ClassroomSummaryResponse.builder()
            .id(classroomId.toString())
            .title("Math 101")
            .description("Basic math")
            .joinCode("ABC123")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .studentCount(5)
            .build();

        when(teacherClassroomService.listClassrooms(teacherId)).thenReturn(List.of(summary));

        List<ClassroomSummaryResponse> result = teacherClassroomController.listClassrooms(principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(classroomId.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Math 101");
        assertThat(result.get(0).getStudentCount()).isEqualTo(5);
    }

    @Test
    void listClassrooms_noClassrooms_returnsEmptyList() {
        UUID teacherId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", teacherId, "TEACHER", null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );

        when(teacherClassroomService.listClassrooms(teacherId)).thenReturn(List.of());

        List<ClassroomSummaryResponse> result = teacherClassroomController.listClassrooms(principal);

        assertThat(result).isEmpty();
    }
}
