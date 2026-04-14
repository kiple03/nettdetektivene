package org.ntnu.backend.level.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class LevelResponse {
    private int id;
    private String title;
    private String description;
    private List<QuestionDto> questions;

    @Getter
    @AllArgsConstructor
    public static class QuestionDto {
        private int id;
        private String text;
        private List<OptionDto> options;
    }

    @Getter
    @AllArgsConstructor
    public static class OptionDto {
        private int id;
        private String text;
        // isCorrect sendes IKKE til frontend
    }
}