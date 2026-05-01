package org.ntnu.backend.notes.model.enums;

/**
 * Categories used to classify detective notes collected during gameplay.
 * Each value corresponds to a thematic investigation area in the game.
 */
public enum NoteCategory {
    /** News/media-related clues. */
    NYHETER,
    /** Image or photo evidence. */
    BILDER,
    /** Email-related evidence. */
    EPOST,
    /** Password and security clues. */
    PASSORD,
    /** General internet/web clues. */
    NETT,
    /** Social media evidence. */
    SOSIALE,
    /** Personal/custom notes added by the student. */
    EGNE
}