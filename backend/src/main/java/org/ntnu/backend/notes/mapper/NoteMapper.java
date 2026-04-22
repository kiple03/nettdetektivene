package org.ntnu.backend.notes.mapper;

import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Maps note domain objects to DTOs used by the notebook API.
 *
 * <p>This mapper converts both personal {@link Note} entities and granted
 * {@link StudentLevelNote} entities into a shared {@link NoteResponse} format
 * used by the frontend.
 *
 * <p>It also builds a {@link NotesOverviewResponse} by grouping notes by their
 * category key so they can be displayed in the correct notebook tabs.
 */
@Component
public class NoteMapper {

    /**
     * Converts a personal note entity into a response DTO.
     *
     * @param note the personal note to convert
     * @return the mapped note response
     */
    public NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId().toString(),
                toFrontendKey(note.getCategory().name()),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    /**
     * Converts a granted level note entity into a response DTO.
     *
     * <p>Since granted notes do not have a separate updated timestamp, the
     * granted time is used for both created and updated fields.
     *
     * @param note the granted level note to convert
     * @return the mapped note response
     */
    public NoteResponse toResponse(StudentLevelNote note) {
        return new NoteResponse(
                note.getId().toString(),
                toFrontendKey(note.getCategory().name()),
                note.getContent(),
                note.getGrantedAt(),
                note.getGrantedAt()
        );
    }

    /**
     * Builds the full notebook overview response for a user.
     *
     * <p>Personal notes and granted level notes are grouped separately by their
     * frontend category keys.
     *
     * @param ownNotes the user's personal notes
     * @param levelNotes the user's granted level notes
     * @return the grouped notebook overview response
     */
    public NotesOverviewResponse toOverviewResponse(
            Iterable<Note> ownNotes,
            Iterable<StudentLevelNote> levelNotes
    ) {
        Map<String, List<NoteResponse>> reflections = StreamSupport
                .stream(ownNotes.spliterator(), false)
                .collect(Collectors.groupingBy(
                        note -> toFrontendKey(note.getCategory().name()),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        Map<String, List<NoteResponse>> levelNotesMap = StreamSupport
                .stream(levelNotes.spliterator(), false)
                .collect(Collectors.groupingBy(
                        note -> toFrontendKey(note.getCategory().name()),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        return new NotesOverviewResponse(reflections, levelNotesMap);
    }

    /**
     * Converts an enum-style category name into the lowercase key expected by
     * the frontend.
     *
     * @param enumName the enum name to convert
     * @return the lowercase frontend category key
     */
    private String toFrontendKey(String enumName) {
        return enumName.toLowerCase(Locale.ROOT);
    }
}