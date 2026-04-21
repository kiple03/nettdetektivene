package org.ntnu.backend.classroom.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.service.TeacherNotificationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/notifications")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherNotificationController {

    private final TeacherNotificationService teacherNotificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return teacherNotificationService.getNotifications(principal.getUserId());
    }
}
