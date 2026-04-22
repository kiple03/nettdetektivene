package org.ntnu.backend.notes.dto;

import java.util.List;
import java.util.Map;

/**
 * Top-level response for a student's full notebook.
 *
 * @param reflections own notes written by the student, grouped by tab
 * @param levelNotes  preset notes granted on level completion, grouped by tab
 */
public record NotesOverviewResponse(
        Map<String, List<NoteResponse>> reflections,
        Map<String, List<NoteResponse>> levelNotes
) {}