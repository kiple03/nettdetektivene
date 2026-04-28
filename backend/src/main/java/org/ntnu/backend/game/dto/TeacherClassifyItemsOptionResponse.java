package org.ntnu.backend.game.dto;

import java.util.List;

/** Full CLASSIFY_ITEMS option data for teachers (adds correctLabel and explanation hidden from students). */
public record TeacherClassifyItemsOptionResponse(
    String key,
    String text,
    List<String> labels,
    String correctLabel,
    String explanation
) implements TeacherOptionResponse {}
