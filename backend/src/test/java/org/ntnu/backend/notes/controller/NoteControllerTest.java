package org.ntnu.backend.notes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.notes.dto.NoteResponse;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.dto.UpsertNoteRequest;
import org.ntnu.backend.notes.service.NoteService;
import org.ntnu.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Test
    void getMyNotes_success() throws Exception {
        NoteResponse ownNote = new NoteResponse(
                UUID.randomUUID().toString(),
                "egne",
                "Min refleksjon",
                Instant.parse("2026-04-22T10:15:30Z"),
                Instant.parse("2026-04-22T10:15:30Z")
        );

        NoteResponse levelNote = new NoteResponse(
                UUID.randomUUID().toString(),
                "nyheter",
                "Dette lærte du i level",
                Instant.parse("2026-04-22T11:15:30Z"),
                Instant.parse("2026-04-22T11:15:30Z")
        );

        NotesOverviewResponse response = new NotesOverviewResponse(
                Map.of("egne", List.of(ownNote)),
                Map.of("nyheter", List.of(levelNote))
        );

        when(noteService.getMyNotes(isNull())).thenReturn(response);

        mockMvc.perform(get("/v1/notes/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reflections.egne[0].id").value(ownNote.id()))
                .andExpect(jsonPath("$.reflections.egne[0].category").value("egne"))
                .andExpect(jsonPath("$.reflections.egne[0].content").value("Min refleksjon"))
                .andExpect(jsonPath("$.levelNotes.nyheter[0].id").value(levelNote.id()))
                .andExpect(jsonPath("$.levelNotes.nyheter[0].category").value("nyheter"))
                .andExpect(jsonPath("$.levelNotes.nyheter[0].content").value("Dette lærte du i level"));

        verify(noteService).getMyNotes(isNull());
    }

    @Test
    void createNote_success() throws Exception {
        NoteResponse response = new NoteResponse(
                UUID.randomUUID().toString(),
                "egne",
                "Dette er en note",
                Instant.parse("2026-04-22T10:15:30Z"),
                Instant.parse("2026-04-22T10:15:30Z")
        );

        when(noteService.createNote(isNull(), eq("egne"), any(UpsertNoteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/v1/notes/me/egne")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Dette er en note"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.category").value("egne"))
                .andExpect(jsonPath("$.content").value("Dette er en note"));

        verify(noteService).createNote(isNull(), eq("egne"), any(UpsertNoteRequest.class));
    }

    @Test
    void createNote_blankContent_throws400() throws Exception {
        mockMvc.perform(post("/v1/notes/me/egne")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNote_tooLongContent_throws400() throws Exception {
        String longContent = "a".repeat(5001);

        mockMvc.perform(post("/v1/notes/me/egne")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "%s"
                                }
                                """.formatted(longContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMyNote_success() throws Exception {
        UUID noteId = UUID.randomUUID();

        mockMvc.perform(delete("/v1/notes/me/{id}", noteId))
                .andExpect(status().isNoContent());

        verify(noteService).deleteMyNote(isNull(), eq(noteId));
    }
}