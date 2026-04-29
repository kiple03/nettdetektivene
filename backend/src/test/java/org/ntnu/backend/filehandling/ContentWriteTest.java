package org.ntnu.backend.filehandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;

class ContentWriteTest {

    @Test
    void write_whenOptionsIsEmpty_shouldReturnEmptyJsonArray() {
        String result = ContentWrite.write(List.of());

        assertThat(result).isEqualTo("[]");
    }

    @Test
    void write_whenMultipleChoiceOptions_shouldReturnJsonArrayContainingFields() {
        List<Option> options = List.of(
                new MultipleChoiceOption(
                        1,
                        "A",
                        "https://example.com/a.jpg",
                        false,
                        "Reason A"
                ),
                new MultipleChoiceOption(
                        2,
                        "B",
                        "https://example.com/b.jpg",
                        true,
                        "Reason B"
                )
        );

        String result = ContentWrite.write(options);

        assertThat(result).contains("\"id\":1");
        assertThat(result).contains("\"text\":\"A\"");
        assertThat(result).contains("\"imageUrl\":\"https://example.com/a.jpg\"");
        assertThat(result).contains("\"correct\":false");
        assertThat(result).contains("\"explanation\":\"Reason A\"");

        assertThat(result).contains("\"id\":2");
        assertThat(result).contains("\"text\":\"B\"");
        assertThat(result).contains("\"imageUrl\":\"https://example.com/b.jpg\"");
        assertThat(result).contains("\"correct\":true");
        assertThat(result).contains("\"explanation\":\"Reason B\"");
    }

    @Test
    void write_whenOptionsIsNull_shouldReturnJsonNull() {
        String result = ContentWrite.write(null);

        assertThat(result).isEqualTo("null");
    }
}