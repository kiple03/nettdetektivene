package org.ntnu.backend.notes.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
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
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final StudentLevelNoteRepository studentLevelNoteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;

    @Transactional(readOnly = true)
    public NotesOverviewResponse getMyNotes(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<Note> ownNotes = noteRepository.findAllByUserOrderByCategoryAsc(user);
        List<StudentLevelNote> levelNotes = studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(user);
        return noteMapper.toOverviewResponse(ownNotes, levelNotes);
    }

    public NoteResponse createNote(Authentication authentication, String categoryRaw, UpsertNoteRequest request) {
        User user = getAuthenticatedUser(authentication);
        NoteCategory category = parseCategory(categoryRaw);

        Note note = Note.builder()
                .user(user)
                .category(category)
                .content(request.content().trim())
                .build();

        return noteMapper.toResponse(noteRepository.save(note));
    }

    public void deleteMyNote(Authentication authentication, UUID noteId) {
        User user = getAuthenticatedUser(authentication);
        noteRepository.deleteByIdAndUser(noteId, user);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(authentication);
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private NoteCategory parseCategory(String raw) {
        try {
            return NoteCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid note category: " + raw);
        }
    }
}