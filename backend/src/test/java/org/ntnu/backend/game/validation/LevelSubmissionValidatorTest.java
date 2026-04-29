package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.exception.InvalidLevelSubmissionException;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.progress.QuestionAnswer;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;

class LevelSubmissionValidatorTest {

    private OptionParser optionParser;
    private SubmittedAnswerValidatorRegistry validatorRegistry;
    private SubmittedAnswerValidator submittedAnswerValidator;

    private LevelSubmissionValidator levelSubmissionValidator;

    @BeforeEach
    void setUp() {
        optionParser = mock(OptionParser.class);
        validatorRegistry = mock(SubmittedAnswerValidatorRegistry.class);
        submittedAnswerValidator = mock(SubmittedAnswerValidator.class);

        levelSubmissionValidator = new LevelSubmissionValidator(
                optionParser,
                validatorRegistry
        );
    }

    @Test
    void validate_whenQuestionsAndAnswersAreBothEmpty_shouldPassWithoutParsingOrValidation() {
        levelSubmissionValidator.validate(List.of(), List.of());

        verifyNoInteractions(optionParser);
        verifyNoInteractions(validatorRegistry);
        verifyNoInteractions(submittedAnswerValidator);
    }

    @Test
    void validate_whenQuestionUsesPictureQuiz_shouldUsePictureQuizValidator() {
        Level level = level(LevelType.PICTURE_QUIZ);

        Question question = question(1, level, "picture-options");

        SubmitAnswerRequest answer = mock(SubmitAnswerRequest.class);
        QuestionAnswer questionAnswer = new QuestionAnswer(1, answer);

        List<Option> options = List.of();

        when(optionParser.parse("picture-options", LevelType.PICTURE_QUIZ))
                .thenReturn(options);
        when(validatorRegistry.forType(LevelType.PICTURE_QUIZ))
                .thenReturn(submittedAnswerValidator);
        when(submittedAnswerValidator.validate(options, answer))
                .thenReturn(correctResponse());

        levelSubmissionValidator.validate(
                List.of(question),
                List.of(questionAnswer)
        );

        verify(optionParser).parse("picture-options", LevelType.PICTURE_QUIZ);
        verify(validatorRegistry).forType(LevelType.PICTURE_QUIZ);
        verify(submittedAnswerValidator).validate(options, answer);
    }

    @Test
    void validate_whenAllAnswersAreCorrect_shouldPass() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");
        Question questionTwo = question(2, level, "options-two");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        SubmitAnswerRequest answerTwo = mock(SubmitAnswerRequest.class);

        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);
        QuestionAnswer questionAnswerTwo = new QuestionAnswer(2, answerTwo);

        List<Option> optionsOne = List.of();
        List<Option> optionsTwo = List.of();

        when(optionParser.parse("options-one", LevelType.MULTIPLE_CHOICE))
                .thenReturn(optionsOne);
        when(optionParser.parse("options-two", LevelType.MULTIPLE_CHOICE))
                .thenReturn(optionsTwo);

        when(validatorRegistry.forType(LevelType.MULTIPLE_CHOICE))
                .thenReturn(submittedAnswerValidator);

        when(submittedAnswerValidator.validate(optionsOne, answerOne))
                .thenReturn(correctResponse());
        when(submittedAnswerValidator.validate(optionsTwo, answerTwo))
                .thenReturn(correctResponse());

        levelSubmissionValidator.validate(
                List.of(questionOne, questionTwo),
                List.of(questionAnswerOne, questionAnswerTwo)
        );

        verify(optionParser).parse("options-one", LevelType.MULTIPLE_CHOICE);
        verify(optionParser).parse("options-two", LevelType.MULTIPLE_CHOICE);
        verify(validatorRegistry, times(2)).forType(LevelType.MULTIPLE_CHOICE);
        verify(submittedAnswerValidator).validate(optionsOne, answerOne);
        verify(submittedAnswerValidator).validate(optionsTwo, answerTwo);
    }

    @Test
    void validate_whenAnswerCountIsTooLow_shouldThrowInvalidLevelSubmissionException() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");
        Question questionTwo = question(2, level, "options-two");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);

        assertThatThrownBy(() -> levelSubmissionValidator.validate(
                List.of(questionOne, questionTwo),
                List.of(questionAnswerOne)
        ))
                .isInstanceOf(InvalidLevelSubmissionException.class)
                .hasMessageContaining("Expected 2 answers, got 1");

        verifyNoInteractions(optionParser);
        verifyNoInteractions(validatorRegistry);
        verifyNoInteractions(submittedAnswerValidator);
    }

    @Test
    void validate_whenAnswerCountIsTooHigh_shouldThrowInvalidLevelSubmissionException() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        SubmitAnswerRequest answerTwo = mock(SubmitAnswerRequest.class);

        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);
        QuestionAnswer questionAnswerTwo = new QuestionAnswer(2, answerTwo);

        assertThatThrownBy(() -> levelSubmissionValidator.validate(
                List.of(questionOne),
                List.of(questionAnswerOne, questionAnswerTwo)
        ))
                .isInstanceOf(InvalidLevelSubmissionException.class)
                .hasMessageContaining("Expected 1 answers, got 2");

        verifyNoInteractions(optionParser);
        verifyNoInteractions(validatorRegistry);
        verifyNoInteractions(submittedAnswerValidator);
    }

    @Test
    void validate_whenSubmittedQuestionIdsDoNotMatch_shouldThrowInvalidLevelSubmissionException() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");
        Question questionTwo = question(2, level, "options-two");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        SubmitAnswerRequest answerThree = mock(SubmitAnswerRequest.class);

        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);
        QuestionAnswer questionAnswerThree = new QuestionAnswer(3, answerThree);

        assertThatThrownBy(() -> levelSubmissionValidator.validate(
                List.of(questionOne, questionTwo),
                List.of(questionAnswerOne, questionAnswerThree)
        ))
                .isInstanceOf(InvalidLevelSubmissionException.class)
                .hasMessageContaining("Submitted question IDs do not match the level's questions");

        verifyNoInteractions(optionParser);
        verifyNoInteractions(validatorRegistry);
        verifyNoInteractions(submittedAnswerValidator);
    }

    @Test
    void validate_whenAnyAnswerIsIncorrect_shouldThrowInvalidLevelSubmissionException() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");
        Question questionTwo = question(2, level, "options-two");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        SubmitAnswerRequest answerTwo = mock(SubmitAnswerRequest.class);

        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);
        QuestionAnswer questionAnswerTwo = new QuestionAnswer(2, answerTwo);

        List<Option> optionsOne = List.of();
        List<Option> optionsTwo = List.of();

        when(optionParser.parse("options-one", LevelType.MULTIPLE_CHOICE))
                .thenReturn(optionsOne);
        when(optionParser.parse("options-two", LevelType.MULTIPLE_CHOICE))
                .thenReturn(optionsTwo);

        when(validatorRegistry.forType(LevelType.MULTIPLE_CHOICE))
                .thenReturn(submittedAnswerValidator);

        when(submittedAnswerValidator.validate(optionsOne, answerOne))
                .thenReturn(correctResponse());
        when(submittedAnswerValidator.validate(optionsTwo, answerTwo))
                .thenReturn(incorrectResponse());

        assertThatThrownBy(() -> levelSubmissionValidator.validate(
                List.of(questionOne, questionTwo),
                List.of(questionAnswerOne, questionAnswerTwo)
        ))
                .isInstanceOf(InvalidLevelSubmissionException.class)
                .hasMessageContaining("Answer to question 2 is incorrect");

        verify(optionParser).parse("options-one", LevelType.MULTIPLE_CHOICE);
        verify(optionParser).parse("options-two", LevelType.MULTIPLE_CHOICE);
        verify(submittedAnswerValidator).validate(optionsOne, answerOne);
        verify(submittedAnswerValidator).validate(optionsTwo, answerTwo);
    }

    @Test
    void validate_whenFirstAnswerIsIncorrect_shouldThrowAndStopValidation() {
        Level level = level(LevelType.MULTIPLE_CHOICE);

        Question questionOne = question(1, level, "options-one");
        Question questionTwo = question(2, level, "options-two");

        SubmitAnswerRequest answerOne = mock(SubmitAnswerRequest.class);
        SubmitAnswerRequest answerTwo = mock(SubmitAnswerRequest.class);

        QuestionAnswer questionAnswerOne = new QuestionAnswer(1, answerOne);
        QuestionAnswer questionAnswerTwo = new QuestionAnswer(2, answerTwo);

        List<Option> optionsOne = List.of();

        when(optionParser.parse("options-one", LevelType.MULTIPLE_CHOICE))
                .thenReturn(optionsOne);

        when(validatorRegistry.forType(LevelType.MULTIPLE_CHOICE))
                .thenReturn(submittedAnswerValidator);

        when(submittedAnswerValidator.validate(optionsOne, answerOne))
                .thenReturn(incorrectResponse());

        assertThatThrownBy(() -> levelSubmissionValidator.validate(
                List.of(questionOne, questionTwo),
                List.of(questionAnswerOne, questionAnswerTwo)
        ))
                .isInstanceOf(InvalidLevelSubmissionException.class)
                .hasMessageContaining("Answer to question 1 is incorrect");

        verify(optionParser).parse("options-one", LevelType.MULTIPLE_CHOICE);
        verify(optionParser, never()).parse("options-two", LevelType.MULTIPLE_CHOICE);
        verify(submittedAnswerValidator).validate(optionsOne, answerOne);
        verifyNoMoreInteractions(submittedAnswerValidator);
    }

    private Level level(LevelType levelType) {
        Level level = mock(Level.class);
        when(level.getLevelType()).thenReturn(levelType);
        return level;
    }

    private Question question(int id, Level level, String optionContent) {
        Question question = mock(Question.class);
        when(question.getId()).thenReturn(id);
        when(question.getLevel()).thenReturn(level);
        when(question.getOptionContent()).thenReturn(optionContent);
        return question;
    }

    private ValidationResult correctResponse() {
        return new ValidationResult(true, null, List.of(), List.of());
    }

    private ValidationResult incorrectResponse() {
        return new ValidationResult(false, "Wrong answer", List.of(), List.of());
    }
}