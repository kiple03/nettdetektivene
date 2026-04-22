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

@Component
public class NoteMapper {

    public NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId().toString(),
                toFrontendKey(note.getCategory().name()),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    public NoteResponse toResponse(StudentLevelNote note) {
        return new NoteResponse(
                note.getId().toString(),
                toFrontendKey(note.getCategory().name()),
                note.getContent(),
                note.getGrantedAt(),
                note.getGrantedAt()
        );
    }

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

    private String toFrontendKey(String enumName) {
        return enumName.toLowerCase(Locale.ROOT);
    }
}