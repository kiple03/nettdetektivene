package org.ntnu.backend.notes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;
import org.ntnu.backend.notes.repository.StudentLevelNoteRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;

@ExtendWith(MockitoExtension.class)
class LevelNoteServiceTest {

    @Mock
    private LevelNoteTemplateRepository templateRepository;
    @Mock
    private StudentLevelNoteRepository studentLevelNoteRepository;

    @InjectMocks
    private LevelNoteService levelNoteService;

    private User testUser;
    private Level testLevel;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .username("testuser")
                .email("test@test.no")
                .role(RoleEnum.STUDENT)
                .build();

        testLevel = new Level();
        testLevel.setId(1);
        testLevel.setTitle("Level 1");
    }

    // ===== GRANT LEVEL NOTES =====

    @Test
    void grantLevelNotes_savesNotesForTemplatesNotAlreadyGranted() {
        LevelNoteTemplate template = LevelNoteTemplate.builder()
                .id(10)
                .level(testLevel)
                .category(NoteCategory.NYHETER)
                .content("Dette er en level note")
                .build();

        when(templateRepository.findAllByLevelId(testLevel.getId())).thenReturn(List.of(template));
        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, template.getId())).thenReturn(false);

        levelNoteService.grantLevelNotes(testUser, testLevel);

        ArgumentCaptor<StudentLevelNote> noteCaptor = ArgumentCaptor.forClass(StudentLevelNote.class);
        verify(studentLevelNoteRepository).save(noteCaptor.capture());

        StudentLevelNote savedNote = noteCaptor.getValue();
        assertThat(savedNote.getUser()).isEqualTo(testUser);
        assertThat(savedNote.getTemplate()).isEqualTo(template);
        assertThat(savedNote.getCategory()).isEqualTo(NoteCategory.NYHETER);
        assertThat(savedNote.getContent()).isEqualTo("Dette er en level note");
    }

    @Test
    void grantLevelNotes_skipsTemplatesAlreadyGranted() {
        LevelNoteTemplate template = LevelNoteTemplate.builder()
                .id(10)
                .level(testLevel)
                .category(NoteCategory.NYHETER)
                .content("Dette er en level note")
                .build();

        when(templateRepository.findAllByLevelId(testLevel.getId())).thenReturn(List.of(template));
        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, template.getId())).thenReturn(true);

        levelNoteService.grantLevelNotes(testUser, testLevel);

        verify(studentLevelNoteRepository, never()).save(any(StudentLevelNote.class));
    }

    @Test
    void grantLevelNotes_savesOnlyTemplatesNotAlreadyGranted() {
        LevelNoteTemplate firstTemplate = LevelNoteTemplate.builder()
                .id(10)
                .level(testLevel)
                .category(NoteCategory.NYHETER)
                .content("Første note")
                .build();

        LevelNoteTemplate secondTemplate = LevelNoteTemplate.builder()
                .id(11)
                .level(testLevel)
                .category(NoteCategory.BILDER)
                .content("Andre note")
                .build();

        when(templateRepository.findAllByLevelId(testLevel.getId()))
                .thenReturn(List.of(firstTemplate, secondTemplate));
        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, firstTemplate.getId()))
                .thenReturn(true);
        when(studentLevelNoteRepository.existsByUserAndTemplateId(testUser, secondTemplate.getId()))
                .thenReturn(false);

        levelNoteService.grantLevelNotes(testUser, testLevel);

        ArgumentCaptor<StudentLevelNote> noteCaptor = ArgumentCaptor.forClass(StudentLevelNote.class);
        verify(studentLevelNoteRepository).save(noteCaptor.capture());

        StudentLevelNote savedNote = noteCaptor.getValue();
        assertThat(savedNote.getUser()).isEqualTo(testUser);
        assertThat(savedNote.getTemplate()).isEqualTo(secondTemplate);
        assertThat(savedNote.getCategory()).isEqualTo(NoteCategory.BILDER);
        assertThat(savedNote.getContent()).isEqualTo("Andre note");
    }

    @Test
    void grantLevelNotes_noTemplates_doesNothing() {
        when(templateRepository.findAllByLevelId(testLevel.getId())).thenReturn(List.of());

        levelNoteService.grantLevelNotes(testUser, testLevel);

        verify(studentLevelNoteRepository, never()).save(any(StudentLevelNote.class));
    }
}