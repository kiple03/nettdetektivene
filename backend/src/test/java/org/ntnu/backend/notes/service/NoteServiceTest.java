package org.ntnu.backend.notes.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.dto.UpsertNoteRequest;
import org.ntnu.backend.notes.mapper.NoteMapper;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;
import org.ntnu.backend.notes.repository.NoteRepository;
import org.ntnu.backend.notes.repository.StudentLevelNoteRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private LevelNoteTemplateRepository levelNoteTemplateRepository;

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private StudentLevelNoteRepository studentLevelNoteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private ProfanityFilterService profanityFilter;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ClassroomCensorWordRepository classroomCensorWordRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

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
                RoleEnum.STUDENT,
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

    @Test
    void createNote_withProfanity_shouldSaveRawContentReturnCensoredAndNotifyTeacher() {
        UpsertNoteRequest request = new UpsertNoteRequest("  faen dette var dårlig  ");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(profanityFilter.containsProfanity(eq("faen dette var dårlig"), anyList())).thenReturn(true);
        when(profanityFilter.censor(eq("faen dette var dårlig"), anyList())).thenReturn("*** dette var dårlig");

        Note savedNote = Note.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.EGNE)
                .content("faen dette var dårlig")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        NoteResponse mapperResponse = new NoteResponse(
                savedNote.getId().toString(),
                "egne",
                "faen dette var dårlig",
                savedNote.getCreatedAt(),
                savedNote.getUpdatedAt()
        );

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        when(noteMapper.toResponse(savedNote)).thenReturn(mapperResponse);

        NoteResponse response = noteService.createNote(authentication, "egne", request);

        assertThat(response.content()).isEqualTo("*** dette var dårlig");

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());

        Note captured = noteCaptor.getValue();
        assertThat(captured.getUser()).isEqualTo(testUser);
        assertThat(captured.getCategory()).isEqualTo(NoteCategory.EGNE);
        assertThat(captured.getContent()).isEqualTo("faen dette var dårlig");

        verify(notificationService).notifyInappropriateNote(testUser);
    }

    @Test
    void createNote_withoutProfanity_shouldNotNotifyTeacher() {
        UpsertNoteRequest request = new UpsertNoteRequest("  vanlig note  ");

        Note savedNote = Note.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.EGNE)
                .content("vanlig note")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        NoteResponse expectedResponse = new NoteResponse(
                savedNote.getId().toString(),
                "egne",
                "vanlig note",
                savedNote.getCreatedAt(),
                savedNote.getUpdatedAt()
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(profanityFilter.containsProfanity(eq("vanlig note"), anyList())).thenReturn(false);
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        when(noteMapper.toResponse(savedNote)).thenReturn(expectedResponse);

        NoteResponse response = noteService.createNote(authentication, "egne", request);

        assertThat(response).isEqualTo(expectedResponse);

        verify(notificationService, never()).notifyInappropriateNote(any(User.class));
    }

    @Test
    void getNotesForUser_success_shouldReturnRawOverview() {
        Note ownNote = Note.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.EGNE)
                .content("faen note")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        StudentLevelNote levelNote = StudentLevelNote.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(NoteCategory.NYHETER)
                .content("vanlig level note")
                .grantedAt(Instant.now())
                .build();

        NoteResponse ownResponse = new NoteResponse(
                ownNote.getId().toString(),
                "egne",
                "faen note",
                ownNote.getCreatedAt(),
                ownNote.getUpdatedAt()
        );

        NoteResponse levelResponse = new NoteResponse(
                levelNote.getId().toString(),
                "nyheter",
                "vanlig level note",
                levelNote.getGrantedAt(),
                levelNote.getGrantedAt()
        );

        NotesOverviewResponse rawResponse = new NotesOverviewResponse(
                Map.of("egne", List.of(ownResponse)),
                Map.of("nyheter", List.of(levelResponse))
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(noteRepository.findAllByUserOrderByCategoryAsc(testUser)).thenReturn(List.of(ownNote));
        when(studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(testUser))
                .thenReturn(List.of(levelNote));
        when(noteMapper.toOverviewResponse(List.of(ownNote), List.of(levelNote)))
                .thenReturn(rawResponse);

        NotesOverviewResponse response = noteService.getNotesForUser(testUser.getId());

        assertThat(response.reflections()).containsKey("egne");
        assertThat(response.reflections().get("egne").getFirst().content()).isEqualTo("faen note");

        assertThat(response.levelNotes()).containsKey("nyheter");
        assertThat(response.levelNotes().get("nyheter").getFirst().content())
                .isEqualTo("vanlig level note");
    }

    @Test
    void getNotesForUser_userNotFound_throws404() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getNotesForUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("User not found");
    }

    @Test
    void saveCompletionTipIfNeeded_shouldSaveNotesForTemplatesThatDoNotAlreadyExist() {
        UUID userId = testUser.getId();

        Level level = new Level();
        level.setId(10);

        Long templateOneId = 1L;
        Long templateTwoId = 2L;

        LevelNoteTemplate templateOne = mock(LevelNoteTemplate.class);
        when(templateOne.getId()).thenReturn(Math.toIntExact(templateOneId));
        when(templateOne.getCategory()).thenReturn(NoteCategory.NYHETER);
        when(templateOne.getContent()).thenReturn("Tips fra nivået");

        LevelNoteTemplate templateTwo = mock(LevelNoteTemplate.class);
        when(templateTwo.getId()).thenReturn(Math.toIntExact(templateTwoId));
        when(templateTwo.getCategory()).thenReturn(NoteCategory.PASSORD);
        when(templateTwo.getContent()).thenReturn("Passordtips");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(levelNoteTemplateRepository.findAllByLevelId(level.getId()))
                .thenReturn(List.of(templateOne, templateTwo));

        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, Math.toIntExact(templateOneId)))
                .thenReturn(false);
        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, Math.toIntExact(templateTwoId)))
                .thenReturn(false);

        noteService.saveCompletionTipIfNeeded(userId, level);

        ArgumentCaptor<StudentLevelNote> captor = ArgumentCaptor.forClass(StudentLevelNote.class);
        verify(studentLevelNoteRepository, times(2)).save(captor.capture());

        List<StudentLevelNote> savedNotes = captor.getAllValues();

        assertThat(savedNotes).hasSize(2);

        assertThat(savedNotes.get(0).getUser()).isEqualTo(testUser);
        assertThat(savedNotes.get(0).getTemplate()).isEqualTo(templateOne);
        assertThat(savedNotes.get(0).getCategory()).isEqualTo(NoteCategory.NYHETER);
        assertThat(savedNotes.get(0).getContent()).isEqualTo("Tips fra nivået");

        assertThat(savedNotes.get(1).getUser()).isEqualTo(testUser);
        assertThat(savedNotes.get(1).getTemplate()).isEqualTo(templateTwo);
        assertThat(savedNotes.get(1).getCategory()).isEqualTo(NoteCategory.PASSORD);
        assertThat(savedNotes.get(1).getContent()).isEqualTo("Passordtips");
    }

    @Test
    void saveCompletionTipIfNeeded_whenNoTemplatesExist_shouldNotSaveAnything() {
        UUID userId = testUser.getId();

        Level level = new Level();
        level.setId(10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(levelNoteTemplateRepository.findAllByLevelId(level.getId()))
                .thenReturn(List.of());

        noteService.saveCompletionTipIfNeeded(userId, level);

        verify(studentLevelNoteRepository, never()).save(any(StudentLevelNote.class));
    }

    @Test
    void saveCompletionTipIfNeeded_whenUserNotFound_throws404() {
        UUID userId = UUID.randomUUID();

        Level level = new Level();
        level.setId(10);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.saveCompletionTipIfNeeded(userId, level))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("User not found");

        verify(levelNoteTemplateRepository, never()).findAllByLevelId(any(Integer.class));
        verify(studentLevelNoteRepository, never()).save(any(StudentLevelNote.class));
    }
}