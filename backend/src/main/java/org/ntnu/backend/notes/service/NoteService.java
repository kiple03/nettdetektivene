package org.ntnu.backend.notes.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
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
import org.springframework.transaction.annotation.Propagation;
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
    private final ClassroomCensorWordRepository classroomCensorWordRepository;
    private final EnrollmentRepository enrollmentRepository;

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
        UUID classroomId = enrollmentRepository.findByUserAndLeftAtIsNull(user)
                .map(e -> e.getClassroom().getId())
                .orElse(null);
        List<Note> ownNotes = noteRepository.findAllByUserOrderByCategoryAsc(user);
        List<StudentLevelNote> levelNotes = studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(
            user);
        NotesOverviewResponse raw = noteMapper.toOverviewResponse(ownNotes, levelNotes);
        return applyCensoring(raw, classroomId);
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

        UUID classroomId = enrollmentRepository.findByUserAndLeftAtIsNull(user)
                .map(e -> e.getClassroom().getId())
                .orElse(null);
        List<String> classroomWords = classroomId == null ? List.of() :
                classroomCensorWordRepository.findByClassroomId(classroomId)
                        .stream().map(ClassroomCensorWord::getWord).toList();
        String content = request.content().trim();
        boolean hasProfanity = profanityFilter.containsProfanity(content, classroomWords);

        Note note = Note.builder()
            .user(user)
            .category(category)
            .content(content)
            .build();

        NoteResponse response = noteMapper.toResponse(noteRepository.save(note));
        if (hasProfanity) {
            notificationService.notifyInappropriateNote(user);
            return new NoteResponse(
                response.id(),
                response.category(),
                profanityFilter.censor(content, classroomWords),
                response.createdAt(),
                response.updatedAt()
            );
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

    /**
     * Returns the notebook for a specific user, with content censored according to the classroom's word list.
     *
     * @param userId      the id of the student whose notes to retrieve
     * @param classroomId the classroom whose censor list applies; may be {@code null}
     * @return the censored notes overview
     */
    @Transactional(readOnly = true)
    public NotesOverviewResponse getNotesForUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Note> ownNotes = noteRepository.findAllByUserOrderByCategoryAsc(user);
        List<StudentLevelNote> levelNotes = studentLevelNoteRepository.findAllByUserOrderByCategoryAsc(user);
        return noteMapper.toOverviewResponse(ownNotes, levelNotes);
    }

    /**
     * Applies the classroom's censor word list to all note content in the overview.
     *
     * @param response    the raw (uncensored) notes overview
     * @param classroomId the classroom whose word list to apply; may be {@code null}
     * @return a new overview with censored content
     */
    private NotesOverviewResponse applyCensoring(NotesOverviewResponse response, UUID classroomId) {
        List<String> classroomWords = classroomId == null ? List.of() :
                classroomCensorWordRepository.findByClassroomId(classroomId)
                        .stream().map(ClassroomCensorWord::getWord).toList();
        return new NotesOverviewResponse(
            censorMap(response.reflections(), classroomWords),
            censorMap(response.levelNotes(), classroomWords)
        );
    }

    /**
     * Censors note content across a category-keyed map of note responses.
     *
     * @param map            notes grouped by category
     * @param classroomWords the words to censor
     * @return a new map with censored note content
     */
    private Map<String, List<NoteResponse>> censorMap(Map<String, List<NoteResponse>> map, List<String> classroomWords) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream()
                    .map(note -> new NoteResponse(
                        note.id(),
                        note.category(),
                        profanityFilter.containsProfanity(note.content(), classroomWords)
                            ? profanityFilter.censor(note.content(), classroomWords)
                            : note.content(),
                        note.createdAt(),
                        note.updatedAt()
                    ))
                    .toList()
            ));
    }

    /**
     * Grants all note templates associated with a completed level to the user, skipping any already awarded.
     * Runs in a new transaction so failures don't roll back the surrounding level-submission transaction.
     *
     * @param userId the id of the student who completed the level
     * @param level  the completed level
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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