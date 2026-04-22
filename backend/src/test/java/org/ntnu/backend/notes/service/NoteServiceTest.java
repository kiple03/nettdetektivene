package org.ntnu.backend.notes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.dto.UpsertNoteRequest;
import org.ntnu.backend.notes.mapper.NoteMapper;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.notes.repository.NoteRepository;
import org.ntnu.backend.notes.repository.StudentLevelNoteRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private StudentLevelNoteRepository studentLevelNoteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteService noteService;

    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@test.no")
                .role(RoleEnum.STUDENT)
                .build();

        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
                testUser.getEmail(),
                testUser.getId(),
                "STUDENT",
                null,
                List.of()
        );

        authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    // ===== GET MY NOTES =====

    @Test
    void getMyNotes_success() {
        Note ownNote = Note.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.EGNE)
                .content("Min note")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        StudentLevelNote levelNote = StudentLevelNote.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.NYHETER)
                .content("Level note")
                .grantedAt(Instant.now())
                .build();

        NotesOverviewResponse expectedResponse = new NotesOverviewResponse(
                java.util.Map.of(),
                java.util.Map.of()
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(noteRepository.findAllByUserOrderByCategoryAsc(testUser)).thenReturn(List.of(ownNote));
        when(studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(testUser))
                .thenReturn(List.of(levelNote));
        when(noteMapper.toOverviewResponse(List.of(ownNote), List.of(levelNote)))
                .thenReturn(expectedResponse);

        NotesOverviewResponse response = noteService.getMyNotes(authentication);

        assertThat(response).isEqualTo(expectedResponse);
        verify(noteRepository).findAllByUserOrderByCategoryAsc(testUser);
        verify(studentLevelNoteRepository).findAllByUserOrderByCategoryAsc(testUser);
        verify(noteMapper).toOverviewResponse(List.of(ownNote), List.of(levelNote));
    }

    @Test
    void getMyNotes_userNotFound_throws404() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getMyNotes(authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("User not found");
    }

    // ===== CREATE NOTE =====

    @Test
    void createNote_success() {
        UpsertNoteRequest request = new UpsertNoteRequest("  Dette er en note  ");

        Note savedNote = Note.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.EGNE)
                .content("Dette er en note")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        NoteResponse expectedResponse = new NoteResponse(
                savedNote.getId().toString(),
                "egne",
                "Dette er en note",
                savedNote.getCreatedAt(),
                savedNote.getUpdatedAt()
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        when(noteMapper.toResponse(savedNote)).thenReturn(expectedResponse);

        NoteResponse response = noteService.createNote(authentication, "egne", request);

        assertThat(response).isEqualTo(expectedResponse);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());

        Note captured = noteCaptor.getValue();
        assertThat(captured.getUser()).isEqualTo(testUser);
        assertThat(captured.getCategory()).isEqualTo(NoteCategory.EGNE);
        assertThat(captured.getContent()).isEqualTo("Dette er en note");
    }

    @Test
    void createNote_invalidCategory_throws400() {
        UpsertNoteRequest request = new UpsertNoteRequest("Test");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> noteService.createNote(authentication, "ugyldig", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST")
                .hasMessageContaining("Invalid note category");
    }

    @Test
    void createNote_userNotFound_throws404() {
        UpsertNoteRequest request = new UpsertNoteRequest("Test");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.createNote(authentication, "egne", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("User not found");
    }

    // ===== DELETE NOTE =====

    @Test
    void deleteMyNote_success() {
        UUID noteId = UUID.randomUUID();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        noteService.deleteMyNote(authentication, noteId);

        verify(noteRepository).deleteByIdAndUser(noteId, testUser);
    }

    @Test
    void deleteMyNote_userNotFound_throws404() {
        UUID noteId = UUID.randomUUID();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.deleteMyNote(authentication, noteId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("User not found");
    }

    // ===== AUTH EDGE CASES =====

    @Test
    void getMyNotes_missingAuthentication_throws401() {
        assertThatThrownBy(() -> noteService.getMyNotes(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED")
                .hasMessageContaining("Authentication required");
    }

    @Test
    void createNote_wrongPrincipalType_throws401() {
        Authentication invalidAuth = new UsernamePasswordAuthenticationToken("not-a-jwt-principal", null);

        assertThatThrownBy(() -> noteService.createNote(invalidAuth, "egne", new UpsertNoteRequest("Test")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED")
                .hasMessageContaining("Authentication required");
    }
}