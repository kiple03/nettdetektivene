package org.ntnu.backend.filehandling;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContentReadTest {

    @Test
    void read_whenJsonIsMalformed_shouldThrowRuntimeException() {
        String malformedJson = """
                [
                  { "id": 1, "text": "A"
                """;

        assertThatThrownBy(() -> ContentRead.read(malformedJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kunne ikke lese options fra JSON");
    }

    @Test
    void read_whenJsonCannotBeMappedToOption_shouldThrowRuntimeException() {
        String json = """
                [
                  {
                    "id": 1,
                    "text": "A",
                    "imageUrl": "https://example.com/a.jpg",
                    "correct": true,
                    "explanation": "Reason A"
                  }
                ]
                """;

        assertThatThrownBy(() -> ContentRead.read(json))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kunne ikke lese options fra JSON");
    }

    @Test
    void read_whenJsonIsNull_shouldThrowRuntimeException() {
        assertThatThrownBy(() -> ContentRead.read(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kunne ikke lese options fra JSON");
    }
}