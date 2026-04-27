package org.ntnu.backend.notes.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.user.service.ProfanityFilterService;
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
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manages the authenticated student's notebook.
 *
 * <p>This service supports reading all notes available to the current user,
 * creating personal notes, and deleting personal notes. The returned notebook
 * combines both student-written notes and preset notes granted through level
 * progression.
 *
 * <p>All operations are scoped to the authenticated user. If authentication
 * is missing or the user cannot be found, the request fails with an HTTP
 * error response.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final StudentLevelNoteRepository studentLevelNoteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;
    private final LevelNoteTemplateRepository levelNoteTemplateRepository;
    private final ProfanityFilterService profanityFilter;
    private final NotificationService notificationService;

    /**
     * Returns the full notebook for the authenticated user, including both personal notes and
     * level-granted notes grouped by category.
     *
     * @param authentication the current authenticated user
     * @return an overview of the user's notebook content
     */
    @Transactional(readOnly = true)
    public NotesOverviewResponse getMyNotes(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<Note> ownNotes = noteRepository.findAllByUserOrderByCategoryAsc(user);
        List<StudentLevelNote> levelNotes = studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(
            user);
        return noteMapper.toOverviewResponse(ownNotes, levelNotes);
    }

    /**
     * Creates a new personal note for the authenticated user in the given category.
     *
     * @param authentication the current authenticated user
     * @param categoryRaw    the category provided by the client
     * @param request        the note content to save
     * @return the saved note as a response DTO
     */
    public NoteResponse createNote(Authentication authentication, String categoryRaw,
        UpsertNoteRequest request) {
        User user = getAuthenticatedUser(authentication);
        NoteCategory category = parseCategory(categoryRaw);

        UUID classroomId = JwtAuthenticatedPrincipal.from(authentication).getClassroomId();
        String content = request.content().trim();
        boolean hasProfanity = profanityFilter.containsProfanity(content, classroomId);
        if (hasProfanity) {
            content = profanityFilter.censor(content, classroomId);
        }

        Note note = Note.builder()
            .user(user)
            .category(category)
            .content(content)
            .build();

        NoteResponse response = noteMapper.toResponse(noteRepository.save(note));
        if (hasProfanity) {
            notificationService.notifyInappropriateNote(user);
        }
        return response;
    }

    /**
     * Deletes one of the authenticated user's personal notes.
     *
     * @param authentication the current authenticated user
     * @param noteId         the id of the note to delete
     */
    public void deleteMyNote(Authentication authentication, UUID noteId) {
        User user = getAuthenticatedUser(authentication);
        noteRepository.deleteByIdAndUser(noteId, user);
    }

    /**
     * Resolves the authenticated user from the Spring Security authentication object and loads the
     * matching user entity from the database.
     *
     * @param authentication the current authentication
     * @return the authenticated user entity
     */
    private User getAuthenticatedUser(Authentication authentication) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(authentication);
        return userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Parses a raw category string into a valid {@link NoteCategory}.
     *
     * @param raw the category value provided by the client
     * @return the parsed note category
     */
    private NoteCategory parseCategory(String raw) {
        try {
            return NoteCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid note category: " + raw);
        }
    }

    @Transactional(readOnly = true)
    public NotesOverviewResponse getNotesForUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Note> ownNotes = noteRepository.findAllByUserOrderByCategoryAsc(user);
        List<StudentLevelNote> levelNotes = studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(user);
        return noteMapper.toOverviewResponse(ownNotes, levelNotes);
    }

    public void saveCompletionTipIfNeeded(UUID userId, Level level) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<LevelNoteTemplate> templates = levelNoteTemplateRepository.findAllByLevelId(level.getId());

        for (LevelNoteTemplate template : templates) {
            boolean alreadyExists = studentLevelNoteRepository.existsByUserAndTemplateId(user,
                template.getId());

            if (alreadyExists) {
                continue;
            }

            StudentLevelNote levelNote = StudentLevelNote.builder()
                .user(user)
                .template(template)
                .category(template.getCategory())
                .content(template.getContent())
                .build();

            studentLevelNoteRepository.save(levelNote);
        }
    }
}