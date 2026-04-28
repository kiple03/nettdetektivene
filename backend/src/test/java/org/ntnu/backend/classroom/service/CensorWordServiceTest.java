package org.ntnu.backend.classroom.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.AddCensorWordRequest;
import org.ntnu.backend.classroom.dto.CensorWordResponse;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CensorWordServiceTest {

    private ClassroomCensorWordRepository censorWordRepository;
    private ClassroomRepository classroomRepository;
    private ClassroomAuthorizationService classroomAuthorizationService;
    private ProfanityFilterService profanityFilterService;
    private JwtAuthenticatedPrincipal principal;

    private CensorWordService censorWordService;

    private UUID classroomId;
    private UUID wordId;
    private Classroom classroom;
    private ClassroomCensorWord wordOne;
    private ClassroomCensorWord wordTwo;

    @BeforeEach
    void setUp() {
        censorWordRepository = mock(ClassroomCensorWordRepository.class);
        classroomRepository = mock(ClassroomRepository.class);
        classroomAuthorizationService = mock(ClassroomAuthorizationService.class);
        profanityFilterService = mock(ProfanityFilterService.class);
        principal = mock(JwtAuthenticatedPrincipal.class);

        censorWordService = new CensorWordService(
                censorWordRepository,
                classroomRepository,
                classroomAuthorizationService,
                profanityFilterService
        );

        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        wordId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        classroom = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .build();

        wordOne = ClassroomCensorWord.builder()
                .id(wordId)
                .classroom(classroom)
                .word("badword")
                .createdAt(Instant.parse("2026-04-28T12:30:00Z"))
                .build();

        wordTwo = ClassroomCensorWord.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .classroom(classroom)
                .word("anotherword")
                .createdAt(Instant.parse("2026-04-28T12:30:00Z"))
                .build();
    }

    @Test
    void listWords_shouldVerifyAccessReturnWordsAndSyncCache() {
        when(censorWordRepository.findByClassroomId(classroomId))
                .thenReturn(List.of(wordOne, wordTwo));

        List<CensorWordResponse> result = censorWordService.listWords(classroomId, principal);

        assertEquals(2, result.size());

        assertEquals(wordId, result.get(0).getId());
        assertEquals("badword", result.get(0).getWord());
        assertEquals(Instant.parse("2026-04-28T12:30:00Z"), result.get(0).getCreatedAt());

        assertEquals("anotherword", result.get(1).getWord());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).findByClassroomId(classroomId);
        verify(profanityFilterService).putClassroomPatterns(
                classroomId,
                List.of("badword", "anotherword")
        );
    }

    @Test
    void listWords_whenNoWords_shouldReturnEmptyListAndSyncEmptyCache() {
        when(censorWordRepository.findByClassroomId(classroomId))
                .thenReturn(List.of());

        List<CensorWordResponse> result = censorWordService.listWords(classroomId, principal);

        assertTrue(result.isEmpty());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).findByClassroomId(classroomId);
        verify(profanityFilterService).putClassroomPatterns(classroomId, List.of());
    }

    @Test
    void addWord_shouldNormalizeSaveReturnResponseAndSyncCache() {
        AddCensorWordRequest request = new AddCensorWordRequest("  BadWord  ");

        when(censorWordRepository.existsByClassroomIdAndWord(classroomId, "badword"))
                .thenReturn(false);
        when(classroomRepository.findById(classroomId))
                .thenReturn(Optional.of(classroom));
        when(censorWordRepository.save(any(ClassroomCensorWord.class)))
                .thenReturn(wordOne);
        when(censorWordRepository.findByClassroomId(classroomId))
                .thenReturn(List.of(wordOne));

        CensorWordResponse result = censorWordService.addWord(classroomId, request, principal);

        assertEquals(wordId, result.getId());
        assertEquals("badword", result.getWord());
        assertEquals(Instant.parse("2026-04-28T12:30:00Z"), result.getCreatedAt());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).existsByClassroomIdAndWord(classroomId, "badword");
        verify(classroomRepository).findById(classroomId);

        verify(censorWordRepository).save(argThat(entity ->
                entity.getClassroom().equals(classroom)
                        && entity.getWord().equals("badword")
        ));

        verify(censorWordRepository).findByClassroomId(classroomId);
        verify(profanityFilterService).putClassroomPatterns(classroomId, List.of("badword"));
    }

    @Test
    void addWord_whenWordAlreadyExists_shouldThrowConflictAndNotSave() {
        AddCensorWordRequest request = new AddCensorWordRequest(" BadWord ");

        when(censorWordRepository.existsByClassroomIdAndWord(classroomId, "badword"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> censorWordService.addWord(classroomId, request, principal)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).existsByClassroomIdAndWord(classroomId, "badword");
        verify(classroomRepository, never()).findById(any());
        verify(censorWordRepository, never()).save(any());
        verify(profanityFilterService, never()).putClassroomPatterns(any(), any());
    }

    @Test
    void addWord_whenClassroomNotFound_shouldThrowNotFoundAndNotSave() {
        AddCensorWordRequest request = new AddCensorWordRequest("badword");

        when(censorWordRepository.existsByClassroomIdAndWord(classroomId, "badword"))
                .thenReturn(false);
        when(classroomRepository.findById(classroomId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> censorWordService.addWord(classroomId, request, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).existsByClassroomIdAndWord(classroomId, "badword");
        verify(classroomRepository).findById(classroomId);
        verify(censorWordRepository, never()).save(any());
        verify(profanityFilterService, never()).putClassroomPatterns(any(), any());
    }

    @Test
    void deleteWord_shouldDeleteWordAndSyncCache() {
        when(censorWordRepository.findByIdAndClassroomId(wordId, classroomId))
                .thenReturn(Optional.of(wordOne));
        when(censorWordRepository.findByClassroomId(classroomId))
                .thenReturn(List.of(wordTwo));

        censorWordService.deleteWord(classroomId, wordId, principal);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).findByIdAndClassroomId(wordId, classroomId);
        verify(censorWordRepository).delete(wordOne);
        verify(censorWordRepository).findByClassroomId(classroomId);
        verify(profanityFilterService).putClassroomPatterns(
                classroomId,
                List.of("anotherword")
        );
    }

    @Test
    void deleteWord_whenWordNotFound_shouldThrowNotFoundAndNotDelete() {
        when(censorWordRepository.findByIdAndClassroomId(wordId, classroomId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> censorWordService.deleteWord(classroomId, wordId, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(censorWordRepository).findByIdAndClassroomId(wordId, classroomId);
        verify(censorWordRepository, never()).delete(any());
        verify(censorWordRepository, never()).findByClassroomId(classroomId);
        verify(profanityFilterService, never()).putClassroomPatterns(any(), any());
    }

    @Test
    void listWords_whenAuthorizationFails_shouldThrowAndNotUseRepository() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No access"))
                .when(classroomAuthorizationService)
                .verifyAccess(principal, classroomId);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> censorWordService.listWords(classroomId, principal)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verifyNoInteractions(censorWordRepository);
        verifyNoInteractions(classroomRepository);
        verifyNoInteractions(profanityFilterService);
    }
}