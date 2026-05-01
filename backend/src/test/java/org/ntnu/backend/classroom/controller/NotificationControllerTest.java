package org.ntnu.backend.classroom.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    private JwtAuthenticationToken teacherAuth(UUID userId) {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", userId, RoleEnum.TEACHER, null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    private NotificationResponse buildResponse(UUID teacherId, UUID classroomId) {
        return NotificationResponse.builder()
            .id(UUID.randomUUID())
            .teacherId(teacherId)
            .classroomId(classroomId)
            .studentId(UUID.randomUUID())
            .type(NotificationType.AREA_COMPLETED)
            .message("test message")
            .read(false)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void getNotifications_returnsAllWhenNoClassroomFilter() throws Exception {
        UUID teacherId = UUID.randomUUID();
        NotificationResponse n = buildResponse(teacherId, UUID.randomUUID());
        when(notificationService.getNotificationsForTeacher(teacherId)).thenReturn(List.of(n));

        mockMvc.perform(get("/teacher/notifications")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getNotifications_filtersWhenClassroomIdProvided() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        NotificationResponse n = buildResponse(teacherId, classroomId);
        when(notificationService.getNotificationsForTeacher(teacherId, classroomId)).thenReturn(List.of(n));

        mockMvc.perform(get("/teacher/notifications")
                .param("classroomId", classroomId.toString())
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].classroomId").value(classroomId.toString()));
    }

    @Test
    void getNotifications_returns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/teacher/notifications"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void markAsRead_returnsUpdatedNotification() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = NotificationResponse.builder()
            .id(notificationId)
            .teacherId(teacherId)
            .classroomId(UUID.randomUUID())
            .studentId(UUID.randomUUID())
            .type(NotificationType.AREA_COMPLETED)
            .message("test")
            .read(true)
            .createdAt(Instant.now())
            .build();

        when(notificationService.markAsRead(eq(notificationId), eq(teacherId))).thenReturn(response);

        mockMvc.perform(patch("/teacher/notifications/{id}/read", notificationId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAsRead_returns404WhenNotificationNotFound() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationService.markAsRead(any(), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/teacher/notifications/{id}/read", notificationId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotification_returns204() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(delete("/teacher/notifications/{id}", notificationId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(notificationId, teacherId);
    }

    @Test
    void deleteNotification_returns404WhenNotificationNotFound() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .when(notificationService).deleteNotification(any(), any());

        mockMvc.perform(delete("/teacher/notifications/{id}", notificationId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isNotFound());
    }
}
