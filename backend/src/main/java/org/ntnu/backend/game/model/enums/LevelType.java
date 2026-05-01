package org.ntnu.backend.game.model.enums;

/** Defines the type of question/challenge presented at a game level. */
public enum LevelType {
    /** A question with one correct answer from several text options. */
    MULTIPLE_CHOICE,
    /** A question answered by typing or selecting text. */
    TEXT_QUIZ,
    /** A question where the answer is chosen from images. */
    PICTURE_QUIZ,
    /** An interactive mini-game challenge. */
    MINIGAME,
    /** A task involving sorting or handling simulated email/post items. */
    POST_OFFICE,
    /** A challenge focused on evaluating or creating a strong password. */
    PASSWORD_STRENGTH,
    /** A drag-and-drop task where items are sorted into categories. */
    CLASSIFY_ITEMS,
    /** The final boss battle challenge for a stop. */
    BOSS_BATTLE,
    /** An introductory tutorial level. */
    TUTORIAL,
}
