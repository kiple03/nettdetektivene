package org.ntnu.backend.notes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.model.enums.NoteCategory;

class NoteMapperTest {

    private NoteMapper noteMapper;

    @BeforeEach
    void setUp() {
        noteMapper = new NoteMapper();
    }

    @Test
    void toResponse_whenPersonalNote_shouldMapAllFieldsAndLowercaseCategory() {
        UUID noteId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-04-28T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-28T11:00:00Z");

        Note note = Note.builder()
                .id(noteId)
                .category(NoteCategory.EGNE)
                .content("Min egen refleksjon")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        NoteResponse result = noteMapper.toResponse(note);

        assertThat(result.id()).isEqualTo(noteId.toString());
        assertThat(result.category()).isEqualTo("egne");
        assertThat(result.content()).isEqualTo("Min egen refleksjon");
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toResponse_whenStudentLevelNote_shouldMapGrantedAtAsCreatedAndUpdated() {
        UUID noteId = UUID.randomUUID();
        Instant grantedAt = Instant.parse("2026-04-28T10:00:00Z");

        StudentLevelNote note = StudentLevelNote.builder()
                .id(noteId)
                .category(NoteCategory.NYHETER)
                .content("Husk å sjekke kilder")
                .grantedAt(grantedAt)
                .build();

        NoteResponse result = noteMapper.toResponse(note);

        assertThat(result.id()).isEqualTo(noteId.toString());
        assertThat(result.category()).isEqualTo("nyheter");
        assertThat(result.content()).isEqualTo("Husk å sjekke kilder");
        assertThat(result.createdAt()).isEqualTo(grantedAt);
        assertThat(result.updatedAt()).isEqualTo(grantedAt);
    }

    @Test
    void toOverviewResponse_shouldGroupPersonalNotesAndLevelNotesByLowercaseCategory() {
        Note ownNoteOne = Note.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.EGNE)
                .content("Egen note 1")
                .createdAt(Instant.parse("2026-04-28T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-28T10:30:00Z"))
                .build();

        Note ownNoteTwo = Note.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.EGNE)
                .content("Egen note 2")
                .createdAt(Instant.parse("2026-04-28T11:00:00Z"))
                .updatedAt(Instant.parse("2026-04-28T11:30:00Z"))
                .build();

        Note ownNoteThree = Note.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.PASSORD)
                .content("Passordnote")
                .createdAt(Instant.parse("2026-04-28T12:00:00Z"))
                .updatedAt(Instant.parse("2026-04-28T12:30:00Z"))
                .build();

        StudentLevelNote levelNoteOne = StudentLevelNote.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.NYHETER)
                .content("Nyhetsnote")
                .grantedAt(Instant.parse("2026-04-28T13:00:00Z"))
                .build();

        StudentLevelNote levelNoteTwo = StudentLevelNote.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.PASSORD)
                .content("Level passordnote")
                .grantedAt(Instant.parse("2026-04-28T14:00:00Z"))
                .build();

        NotesOverviewResponse result = noteMapper.toOverviewResponse(
                List.of(ownNoteOne, ownNoteTwo, ownNoteThree),
                List.of(levelNoteOne, levelNoteTwo)
        );

        Map<String, List<NoteResponse>> reflections = result.reflections();
        Map<String, List<NoteResponse>> levelNotes = result.levelNotes();

        assertThat(reflections).containsOnlyKeys("egne", "passord");
        assertThat(reflections.get("egne")).hasSize(2);
        assertThat(reflections.get("egne"))
                .extracting(NoteResponse::content)
                .containsExactlyInAnyOrder("Egen note 1", "Egen note 2");

        assertThat(reflections.get("passord")).hasSize(1);
        assertThat(reflections.get("passord").getFirst().content()).isEqualTo("Passordnote");

        assertThat(levelNotes).containsOnlyKeys("nyheter", "passord");
        assertThat(levelNotes.get("nyheter")).hasSize(1);
        assertThat(levelNotes.get("nyheter").getFirst().content()).isEqualTo("Nyhetsnote");

        assertThat(levelNotes.get("passord")).hasSize(1);
        assertThat(levelNotes.get("passord").getFirst().content()).isEqualTo("Level passordnote");
    }

    @Test
    void toOverviewResponse_whenInputsAreEmpty_shouldReturnEmptyMaps() {
        NotesOverviewResponse result = noteMapper.toOverviewResponse(
                List.of(),
                List.of()
        );

        assertThat(result.reflections()).isEmpty();
        assertThat(result.levelNotes()).isEmpty();
    }

    @Test
    void toOverviewResponse_shouldKeepPersonalNotesAndLevelNotesSeparateEvenSameCategory() {
        Note ownNote = Note.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.NYHETER)
                .content("Personlig nyhetsnote")
                .createdAt(Instant.parse("2026-04-28T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-28T11:00:00Z"))
                .build();

        StudentLevelNote levelNote = StudentLevelNote.builder()
                .id(UUID.randomUUID())
                .category(NoteCategory.NYHETER)
                .content("Level nyhetsnote")
                .grantedAt(Instant.parse("2026-04-28T12:00:00Z"))
                .build();

        NotesOverviewResponse result = noteMapper.toOverviewResponse(
                List.of(ownNote),
                List.of(levelNote)
        );

        assertThat(result.reflections()).containsOnlyKeys("nyheter");
        assertThat(result.levelNotes()).containsOnlyKeys("nyheter");

        assertThat(result.reflections().get("nyheter")).hasSize(1);
        assertThat(result.reflections().get("nyheter").getFirst().content())
                .isEqualTo("Personlig nyhetsnote");

        assertThat(result.levelNotes().get("nyheter")).hasSize(1);
        assertThat(result.levelNotes().get("nyheter").getFirst().content())
                .isEqualTo("Level nyhetsnote");
    }
}