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

@RestController
@RequestMapping("/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public NotesOverviewResponse getMyNotes(Authentication authentication) {
        return noteService.getMyNotes(authentication);
    }

    @PostMapping("/me/{category}")
    public NoteResponse createNote(Authentication auth, @PathVariable String category, @Valid @RequestBody UpsertNoteRequest request) {
        return noteService.createNote(auth, category, request);
    }

    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyNote(Authentication auth, @PathVariable UUID id) {
        noteService.deleteMyNote(auth, id);
    }
}