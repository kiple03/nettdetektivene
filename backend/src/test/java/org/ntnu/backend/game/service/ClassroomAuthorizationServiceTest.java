package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ClassroomAuthorizationServiceTest {

    @Mock
    private ClassroomTeacherRepository classroomTeacherRepository;

    @InjectMocks
    private ClassroomAuthorizationService classroomAuthorizationService;

    @Test
    void verifyAccess_whenStudentAccessesOwnClassroom_shouldAllow() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = mock(JwtAuthenticatedPrincipal.class);
        when(principal.isStudent()).thenReturn(true);
        when(principal.getClassroomId()).thenReturn(classroomId);

        classroomAuthorizationService.verifyAccess(principal, classroomId);

        verify(principal).isStudent();
        verify(principal).getClassroomId();
        verifyNoInteractions(classroomTeacherRepository);
    }

    @Test
    void verifyAccess_whenStudentAccessesAnotherClassroom_shouldThrowAccessDenied() {
        UUID ownClassroomId = UUID.randomUUID();
        UUID otherClassroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = mock(JwtAuthenticatedPrincipal.class);
        when(principal.isStudent()).thenReturn(true);
        when(principal.getClassroomId()).thenReturn(ownClassroomId);

        assertThatThrownBy(() -> classroomAuthorizationService.verifyAccess(principal, otherClassroomId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this classroom");

        verifyNoInteractions(classroomTeacherRepository);
    }

    @Test
    void verifyAccess_whenTeacherOwnsClassroom_shouldAllow() {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = mock(JwtAuthenticatedPrincipal.class);
        when(principal.isStudent()).thenReturn(false);
        when(principal.isTeacher()).thenReturn(true);
        when(principal.getUserId()).thenReturn(teacherId);

        when(classroomTeacherRepository.existsByClassroomIdAndUserId(classroomId, teacherId))
                .thenReturn(true);

        classroomAuthorizationService.verifyAccess(principal, classroomId);

        verify(classroomTeacherRepository).existsByClassroomIdAndUserId(classroomId, teacherId);
    }

    @Test
    void verifyAccess_whenTeacherDoesNotOwnClassroom_shouldThrowAccessDenied() {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = mock(JwtAuthenticatedPrincipal.class);
        when(principal.isStudent()).thenReturn(false);
        when(principal.isTeacher()).thenReturn(true);
        when(principal.getUserId()).thenReturn(teacherId);

        when(classroomTeacherRepository.existsByClassroomIdAndUserId(classroomId, teacherId))
                .thenReturn(false);

        assertThatThrownBy(() -> classroomAuthorizationService.verifyAccess(principal, classroomId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this classroom");

        verify(classroomTeacherRepository).existsByClassroomIdAndUserId(classroomId, teacherId);
    }

    @Test
    void verifyAccess_whenPrincipalIsNeitherStudentNorTeacher_shouldDoNothing() {
        UUID classroomId = UUID.randomUUID();

        JwtAuthenticatedPrincipal principal = mock(JwtAuthenticatedPrincipal.class);
        when(principal.isStudent()).thenReturn(false);
        when(principal.isTeacher()).thenReturn(false);

        classroomAuthorizationService.verifyAccess(principal, classroomId);

        verifyNoInteractions(classroomTeacherRepository);
    }
}