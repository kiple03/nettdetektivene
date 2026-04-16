package org.ntnu.backend.classroom.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeacherNotificationsController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class TeacherNotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherClassroomService teacherClassroomService;

    @MockitoBean
    private JwtService jwtService;

    private JwtAuthenticationToken teacherAuth(UUID userId) {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", userId, "TEACHER", null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    @Test
    void getNotifications_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        Instant ts = Instant.parse("2025-03-01T10:00:00Z");

        NotificationResponse notif = NotificationResponse.builder()
            .type("LEVEL_COMPLETED")
            .message("alice completed a level in Math 101")
            .classroomId(classroomId.toString())
            .studentUsername("alice")
            .timestamp(ts)
            .build();

        when(teacherClassroomService.getNotifications(teacherId)).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/teacher/notifications")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("LEVEL_COMPLETED"))
            .andExpect(jsonPath("$[0].studentUsername").value("alice"))
            .andExpect(jsonPath("$[0].classroomId").value(classroomId.toString()))
            .andExpect(jsonPath("$[0].message").value("alice completed a level in Math 101"));
    }

    @Test
    void getNotifications_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/teacher/notifications"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotifications_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, "STUDENT", UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/api/teacher/notifications")
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void getNotifications_emptyList_returnsEmptyArray() throws Exception {
        UUID teacherId = UUID.randomUUID();

        when(teacherClassroomService.getNotifications(teacherId)).thenReturn(List.of());

        mockMvc.perform(get("/api/teacher/notifications")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getNotifications_returnsMultipleTypesSortedByTimestamp() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        Instant earlier = Instant.parse("2025-02-01T10:00:00Z");
        Instant later = Instant.parse("2025-03-01T10:00:00Z");

        List<NotificationResponse> notifs = List.of(
            NotificationResponse.builder()
                .type("MYSTERY_SUBMISSION")
                .message("bob submitted a mystery answer in Science")
                .classroomId(classroomId.toString())
                .studentUsername("bob")
                .timestamp(later)
                .build(),
            NotificationResponse.builder()
                .type("LEVEL_COMPLETED")
                .message("alice completed a level in Math 101")
                .classroomId(classroomId.toString())
                .studentUsername("alice")
                .timestamp(earlier)
                .build()
        );

        when(teacherClassroomService.getNotifications(teacherId)).thenReturn(notifs);

        mockMvc.perform(get("/api/teacher/notifications")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("MYSTERY_SUBMISSION"))
            .andExpect(jsonPath("$[1].type").value("LEVEL_COMPLETED"));
    }
}
