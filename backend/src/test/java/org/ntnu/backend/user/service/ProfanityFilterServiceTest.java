package org.ntnu.backend.user.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfanityFilterServiceTest {

    private ProfanityFilterService profanityFilterService;

    @BeforeEach
    void setUp() throws Exception {
        profanityFilterService = new ProfanityFilterService();
        profanityFilterService.init();
    }

    @Test
    void containsProfanity_whenTextIsNull_shouldReturnFalse() {
        assertFalse(profanityFilterService.containsProfanity(null));
    }

    @Test
    void containsProfanity_whenTextIsBlank_shouldReturnFalse() {
        assertFalse(profanityFilterService.containsProfanity("   "));
    }

    @Test
    void containsProfanity_whenTextContainsDefaultProfanity_shouldReturnTrue() {
        assertTrue(profanityFilterService.containsProfanity("Dette var faen ikke bra"));
    }

    @Test
    void containsProfanity_shouldBeCaseInsensitive() {
        assertTrue(profanityFilterService.containsProfanity("Dette var FAEN ikke bra"));
    }

    @Test
    void containsProfanity_shouldOnlyMatchWholeWords() {
        assertFalse(profanityFilterService.containsProfanity("classic"));
    }

    @Test
    void containsProfanity_whenTextDoesNotContainProfanity_shouldReturnFalse() {
        assertFalse(profanityFilterService.containsProfanity("Dette er en helt vanlig tekst"));
    }

    @Test
    void containsProfanity_withExtraWords_whenExtraWordExists_shouldReturnTrue() {
        assertTrue(profanityFilterService.containsProfanity(
                "Dette er en spoiler",
                List.of("spoiler")
        ));
    }

    @Test
    void containsProfanity_withExtraWords_whenExtraWordHasDifferentCase_shouldReturnTrue() {
        assertTrue(profanityFilterService.containsProfanity(
                "Dette er en SPOILER",
                List.of("spoiler")
        ));
    }

    @Test
    void containsProfanity_withExtraWords_shouldOnlyMatchWholeWords() {
        assertFalse(profanityFilterService.containsProfanity(
                "badge",
                List.of("bad")
        ));
    }

    @Test
    void containsProfanity_withExtraWords_whenExtraWordsIsNull_shouldReturnFalseIfNoDefaultProfanity() {
        assertFalse(profanityFilterService.containsProfanity(
                "Dette er en vanlig tekst",
                (List<String>) null
        ));
    }

    @Test
    void containsProfanity_withExtraWords_whenExtraWordsIsEmpty_shouldReturnFalseIfNoDefaultProfanity() {
        assertFalse(profanityFilterService.containsProfanity(
                "Dette er en vanlig tekst",
                List.of()
        ));
    }

    @Test
    void containsProfanity_withExtraWords_whenDefaultProfanityExists_shouldReturnTrue() {
        assertTrue(profanityFilterService.containsProfanity(
                "Dette var faen",
                List.of("spoiler")
        ));
    }

    @Test
    void censor_whenTextIsNull_shouldReturnNull() {
        assertNull(profanityFilterService.censor(null));
    }

    @Test
    void censor_whenTextContainsDefaultProfanity_shouldReplaceWithStars() {
        String result = profanityFilterService.censor("Dette var faen dårlig");

        assertEquals("Dette var *** dårlig", result);
    }

    @Test
    void censor_shouldBeCaseInsensitive() {
        String result = profanityFilterService.censor("Dette var FAEN dårlig");

        assertEquals("Dette var *** dårlig", result);
    }

    @Test
    void censor_whenTextDoesNotContainProfanity_shouldReturnOriginalText() {
        String text = "Dette er en vanlig tekst";

        String result = profanityFilterService.censor(text);

        assertEquals(text, result);
    }

    @Test
    void censor_withExtraWords_whenExtraWordExists_shouldReplaceExtraWord() {
        String result = profanityFilterService.censor("Dette er en spoiler", List.of("spoiler"));

        assertEquals("Dette er en ***", result);
    }

    @Test
    void censor_withExtraWords_whenExtraWordsIsNull_shouldOnlyCensorDefaultProfanity() {
        String result = profanityFilterService.censor("Dette er en spoiler", (List<String>) null);

        assertEquals("Dette er en spoiler", result);
    }

    @Test
    void censor_withExtraWords_whenDefaultAndExtraProfanityExist_shouldCensorBoth() {
        String result = profanityFilterService.censor("Faen, dette er en spoiler", List.of("spoiler"));

        assertEquals("***, dette er en ***", result);
    }
}
