package org.ntnu.backend.classroom.controller;

import java.util.List;
import java.util.UUID;

import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teacher/notifications")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam(required = false) UUID classroomId,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        if (classroomId == null) {
            return notificationService.getNotificationsForTeacher(principal.getUserId());
        }
        return notificationService.getNotificationsForTeacher(principal.getUserId(), classroomId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return notificationService.markAsRead(id, principal.getUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        notificationService.deleteNotification(id, principal.getUserId());
    }
}
