package org.ntnu.backend.level.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.level.dto.*;
import org.ntnu.backend.level.model.Level;
import org.ntnu.backend.level.model.Option;
import org.ntnu.backend.level.model.Question;
import org.ntnu.backend.level.repository.LevelRepository;
import org.ntnu.backend.level.repository.OptionRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelServiceTest {

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private LevelService levelService;

    private Level level;
    private Option correctOption;
    private Option wrongOption;

    @BeforeEach
    void setUp() {
        level = new Level();
        level.setId(1);
        level.setTitle("Nivå 1");
        level.setDescription("Løs e-postene");

        Question question = new Question();
        question.setId(1);
        question.setText("Hva er phishing?");
        question.setLevel(level);

        correctOption = new Option();
        correctOption.setId(1);
        correctOption.setText("Et forsøk på å lure deg til å gi fra deg info");
        correctOption.setCorrect(true);
        correctOption.setQuestion(question);

        wrongOption = new Option();
        wrongOption.setId(2);
        wrongOption.setText("En type fisk");
        wrongOption.setCorrect(false);
        wrongOption.setQuestion(question);

        question.setOptions(List.of(correctOption, wrongOption));
        level.setQuestions(List.of(question));
    }

    @Test
    void getLevel_shouldReturnLevel_whenExists() {
        when(levelRepository.findById(1)).thenReturn(Optional.of(level));

        LevelResponse response = levelService.getLevel(1);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getTitle()).isEqualTo("Nivå 1");
        assertThat(response.getQuestions()).hasSize(1);
        assertThat(response.getQuestions().get(0).getOptions()).hasSize(2);
    }

    @Test
    void getLevel_shouldThrow_whenNotFound() {
        when(levelRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> levelService.getLevel(99))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void validateAnswer_shouldReturnCorrect_whenAnswerIsRight() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("1");

        when(optionRepository.findByIdAndQuestion_Level_Id(1, 1))
                .thenReturn(Optional.of(correctOption));

        ValidateLevelAnswerResponse response = levelService.validateAnswer(1, request);

        assertThat(response.isCorrect()).isTrue();
        assertThat(response.isUnlockedNextLevel()).isTrue();
    }

    @Test
    void validateAnswer_shouldReturnWrong_whenAnswerIsWrong() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("2");

        when(optionRepository.findByIdAndQuestion_Level_Id(2, 1))
                .thenReturn(Optional.of(wrongOption));

        ValidateLevelAnswerResponse response = levelService.validateAnswer(1, request);

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.isUnlockedNextLevel()).isFalse();
    }

    @Test
    void validateAnswer_shouldThrow_whenOptionNotFound() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("99");

        when(optionRepository.findByIdAndQuestion_Level_Id(99, 1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> levelService.validateAnswer(1, request))
                .isInstanceOf(ResponseStatusException.class);
    }
}