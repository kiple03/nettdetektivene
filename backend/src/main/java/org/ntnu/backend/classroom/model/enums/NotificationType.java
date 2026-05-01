package org.ntnu.backend.classroom.model.enums;

/**
 * Categories of teacher notifications generated during classroom activity.
 */
public enum NotificationType {
    /** A student finished all levels in a stop/area. */
    AREA_COMPLETED,
    /** A student completed the entire game. */
    GAME_COMPLETED,
    /** A student has been on the same question for an unusually long time. */
    STUDENT_STUCK,
    /** A student's chosen username was flagged by the profanity filter. */
    INAPPROPRIATE_USERNAME,
    /** A student's submitted note was flagged by the profanity filter. */
    INAPPROPRIATE_NOTE
}
