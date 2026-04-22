package org.ntnu.backend.notes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.dto.UpsertNoteRequest;
import org.ntnu.backend.notes.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for the authenticated user's notebook.
 *
 * <p>This controller exposes endpoints for retrieving all notebook content,
 * creating personal notes in a given category, and deleting personal notes
 * owned by the current user.
 */
@RestController
@RequestMapping("/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Returns the full notebook for the authenticated user.
     *
     * @param authentication the current authenticated user
     * @return an overview of the user's notebook content
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public NotesOverviewResponse getMyNotes(Authentication authentication) {
        return noteService.getMyNotes(authentication);
    }

    /**
     * Creates a new personal note for the authenticated user in the given
     * category.
     *
     * @param auth the current authenticated user
     * @param category the category to create the note in
     * @param request the note content to save
     * @return the created note
     */
    @PostMapping("/me/{category}")
    public NoteResponse createNote(Authentication auth, @PathVariable String category, @Valid @RequestBody UpsertNoteRequest request) {
        return noteService.createNote(auth, category, request);
    }

    /**
     * Deletes one of the authenticated user's personal notes.
     *
     * @param auth the current authenticated user
     * @param id the id of the note to delete
     */
    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyNote(Authentication auth, @PathVariable UUID id) {
        noteService.deleteMyNote(auth, id);
    }
}