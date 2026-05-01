package org.ntnu.backend.game.dto.teacher;

import java.util.List;

/**
 * Full CLASSIFY_ITEMS option data for teachers, extending the student view with
 * the correct answer and an explanation that are hidden from students.
 *
 * @param key          unique identifier for this option
 * @param text         display text shown to the student
 * @param labels       list of category labels the student can choose from
 * @param correctLabel the label that is the correct answer for this item
 * @param explanation  teacher-facing rationale for why correctLabel is correct
 */
public record TeacherClassifyItemsOptionResponse(
    String key,
    String text,
    List<String> labels,
    String correctLabel,
    String explanation
) implements TeacherOptionResponse {}
