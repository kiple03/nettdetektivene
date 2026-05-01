package org.ntnu.backend.notes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.dto.UpsertNoteRequest;
import org.ntnu.backend.notes.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Notes", description = "Student notebook - personal notes and level summaries")
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
    @Operation(summary = "Get the authenticated user's full notebook")
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
    @Operation(
        summary = "Create a personal note in a category",
        description = "Category corresponds to a stop name, e.g. 'NYHETSKVARTALET'"
    )
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
    @Operation(summary = "Delete a personal note owned by the authenticated user")
    @ApiResponse(responseCode = "204", description = "Note deleted")
    @ApiResponse(responseCode = "404", description = "Note not found")
    public void deleteMyNote(Authentication auth, @PathVariable UUID id) {
        noteService.deleteMyNote(auth, id);
    }
}