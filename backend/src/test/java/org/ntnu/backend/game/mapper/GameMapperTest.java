package org.ntnu.backend.game.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.dto.PasswordStrengthOptionResponse;
import org.ntnu.backend.game.dto.PictureQuizOptionResponse;
import org.ntnu.backend.game.dto.PostOfficeOptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.PostOfficeQuestion;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;

class GameMapperTest {

    private OptionParser optionParser;
    private GameMapper gameMapper;

    @BeforeEach
    void setUp() {
        optionParser = mock(OptionParser.class);
        gameMapper = new GameMapper(optionParser);
    }

    @Test
    void toStopOverviewResponse_shouldMapStopAndLockedState() {
        Stop stop = new Stop();
        stop.setId(1);
        stop.setName("Nyhetskvartalet");

        StopResponse result = gameMapper.toStopOverviewResponse(stop, true);

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("Nyhetskvartalet");
        assertThat(result.locked()).isTrue();
    }

    @Test
    void toStopOverviewResponse_whenUnlocked_shouldMapLockedFalse() {
        Stop stop = new Stop();
        stop.setId(1);
        stop.setName("Nyhetskvartalet");

        Level level = new Level();
        level.setId(11);
        level.setTitle("Passordstyrke");
        level.setDescription("Lag et sterkt passord");
        level.setLevelType(LevelType.PASSWORD_STRENGTH);
        level.setOrder(1);
        level.setStop(stop);
    }

    @Test
    void toLevelResponse_shouldMapLevelAndLockedState() {
        Stop stop = new Stop();
        stop.setId(1);

        Level level = new Level();
        level.setId(10);
        level.setTitle("Ekte eller falskt?");
        level.setDescription("Finn den ekte nyheten");
        level.setLevelType(LevelType.MULTIPLE_CHOICE);
        level.setOrder(3);
        level.setStop(stop);

        LevelResponse result = gameMapper.toLevelResponse(level, true);

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.title()).isEqualTo("Ekte eller falskt?");
        assertThat(result.description()).isEqualTo("Finn den ekte nyheten");
        assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
        assertThat(result.order()).isEqualTo(3);
        assertThat(result.locked()).isTrue();
    }

    @Test
    void toLevelResponse_whenUnlocked_shouldMapLockedFalse() {
        Stop stop = new Stop();
        stop.setId(1);
        stop.setName("Nyhetskvartalet");

        Level level = new Level();
        level.setId(11);
        level.setTitle("Passordstyrke");
        level.setDescription("Lag et sterkt passord");
        level.setLevelType(LevelType.PASSWORD_STRENGTH);
        level.setOrder(1);
        level.setStop(stop);

        LevelResponse result = gameMapper.toLevelResponse(level, false);

        assertThat(result.id()).isEqualTo(11);
        assertThat(result.title()).isEqualTo("Passordstyrke");
        assertThat(result.description()).isEqualTo("Lag et sterkt passord");
        assertThat(result.levelType()).isEqualTo(LevelType.PASSWORD_STRENGTH);
        assertThat(result.order()).isEqualTo(1);
        assertThat(result.locked()).isFalse();
    }

    @Test
    void toQuestionResponse_whenMultipleChoice_shouldMapQuestionAndHideCorrectAnswerData() {
        Level level = level(LevelType.MULTIPLE_CHOICE);
        Question question = question(100, level, "Hva er riktig?", "multiple-choice-json");

        MultipleChoiceOption optionOne = mock(MultipleChoiceOption.class);
        when(optionOne.getId()).thenReturn(1);
        when(optionOne.getText()).thenReturn("Alternativ A");
        when(optionOne.getImageUrl()).thenReturn("https://example.com/a.jpg");

        MultipleChoiceOption optionTwo = mock(MultipleChoiceOption.class);
        when(optionTwo.getId()).thenReturn(2);
        when(optionTwo.getText()).thenReturn("Alternativ B");
        when(optionTwo.getImageUrl()).thenReturn(null);

        when(optionParser.parse("multiple-choice-json", LevelType.MULTIPLE_CHOICE))
                .thenReturn(List.of(optionOne, optionTwo));

        QuestionResponse result = gameMapper.toQuestionResponse(question);

        assertThat(result.id()).isEqualTo(100);
        assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
        assertThat(result.questionText()).isEqualTo("Hva er riktig?");
        assertThat(result.email()).isNull();
        assertThat(result.options()).hasSize(2);

        OptionResponse first = result.options().get(0);
        assertThat(first).isInstanceOf(MultipleChoiceOptionResponse.class);

        MultipleChoiceOptionResponse firstMc = (MultipleChoiceOptionResponse) first;
        assertThat(firstMc.id()).isEqualTo(1);
        assertThat(firstMc.text()).isEqualTo("Alternativ A");
        assertThat(firstMc.imageUrl()).isEqualTo("https://example.com/a.jpg");

        OptionResponse second = result.options().get(1);
        assertThat(second).isInstanceOf(MultipleChoiceOptionResponse.class);

        MultipleChoiceOptionResponse secondMc = (MultipleChoiceOptionResponse) second;
        assertThat(secondMc.id()).isEqualTo(2);
        assertThat(secondMc.text()).isEqualTo("Alternativ B");
        assertThat(secondMc.imageUrl()).isNull();

        verify(optionParser).parse("multiple-choice-json", LevelType.MULTIPLE_CHOICE);
    }

    @Test
    void toQuestionResponse_whenPictureQuiz_shouldMapPictureQuizOptions() {
        Level level = level(LevelType.PICTURE_QUIZ);
        Question question = question(101, level, "Hvilket bilde er KI?", "picture-json");

        PictureQuizOption option = mock(PictureQuizOption.class);
        when(option.id()).thenReturn(7);
        when(option.text()).thenReturn("AI-generated");
        when(option.imageUrl()).thenReturn("https://example.com/picture.jpg");

        when(optionParser.parse("picture-json", LevelType.PICTURE_QUIZ))
                .thenReturn(List.of(option));

        QuestionResponse result = gameMapper.toQuestionResponse(question);

        assertThat(result.id()).isEqualTo(101);
        assertThat(result.levelType()).isEqualTo(LevelType.PICTURE_QUIZ);
        assertThat(result.questionText()).isEqualTo("Hvilket bilde er KI?");
        assertThat(result.email()).isNull();
        assertThat(result.options()).hasSize(1);
        assertThat(result.options().getFirst()).isInstanceOf(PictureQuizOptionResponse.class);

        PictureQuizOptionResponse response = (PictureQuizOptionResponse) result.options().getFirst();
        assertThat(response.id()).isEqualTo(7);
        assertThat(response.text()).isEqualTo("AI-generated");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/picture.jpg");

        verify(optionParser).parse("picture-json", LevelType.PICTURE_QUIZ);
    }

    @Test
    void toQuestionResponse_whenPasswordStrength_shouldMapPasswordRulesWithoutFeedback() {
        Level level = level(LevelType.PASSWORD_STRENGTH);
        Question question = question(102, level, "Lag et sterkt passord", "password-json");

        PasswordStrengthOption option = mock(PasswordStrengthOption.class);
        when(option.getId()).thenReturn(3);
        when(option.getMinLength()).thenReturn(8);
        when(option.isRequireDigit()).thenReturn(true);
        when(option.isRequireUpper()).thenReturn(true);
        when(option.isRequireLower()).thenReturn(true);
        when(option.isRequireSymbol()).thenReturn(false);

        when(optionParser.parse("password-json", LevelType.PASSWORD_STRENGTH))
                .thenReturn(List.of(option));

        QuestionResponse result = gameMapper.toQuestionResponse(question);

        assertThat(result.id()).isEqualTo(102);
        assertThat(result.levelType()).isEqualTo(LevelType.PASSWORD_STRENGTH);
        assertThat(result.questionText()).isEqualTo("Lag et sterkt passord");
        assertThat(result.email()).isNull();
        assertThat(result.options()).hasSize(1);
        assertThat(result.options().getFirst()).isInstanceOf(PasswordStrengthOptionResponse.class);

        PasswordStrengthOptionResponse response =
                (PasswordStrengthOptionResponse) result.options().getFirst();

        assertThat(response.id()).isEqualTo(3);
        assertThat(response.minLength()).isEqualTo(8);
        assertThat(response.requireDigit()).isTrue();
        assertThat(response.requireUpper()).isTrue();
        assertThat(response.requireLower()).isTrue();
        assertThat(response.requireSymbol()).isFalse();

        verify(optionParser).parse("password-json", LevelType.PASSWORD_STRENGTH);
    }

    @Test
    void toQuestionResponse_whenPostOffice_shouldMapPostOfficeOptionsAndEmail() {
        Level level = level(LevelType.POST_OFFICE);
        Question question = question(103, level, "Finn mistenkelige deler", "post-office-json");

        PostOfficeOption senderOption = mock(PostOfficeOption.class);
        when(senderOption.getId()).thenReturn("sender");
        when(senderOption.getType()).thenReturn("SENDER");

        PostOfficeOption linkOption = mock(PostOfficeOption.class);
        when(linkOption.getId()).thenReturn("link");
        when(linkOption.getType()).thenReturn("LINK");

        PostOfficeQuestion parsedQuestion = mock(PostOfficeQuestion.class);
        PostOfficeQuestion.Email email = mock(PostOfficeQuestion.Email.class);
        when(parsedQuestion.getEmail()).thenReturn(email);

        when(optionParser.parse("post-office-json", LevelType.POST_OFFICE))
                .thenReturn(List.of(senderOption, linkOption));
        when(optionParser.parsePostOfficeQuestion("post-office-json"))
                .thenReturn(parsedQuestion);

        QuestionResponse result = gameMapper.toQuestionResponse(question);

        assertThat(result.id()).isEqualTo(103);
        assertThat(result.levelType()).isEqualTo(LevelType.POST_OFFICE);
        assertThat(result.questionText()).isEqualTo("Finn mistenkelige deler");
        assertThat(result.email()).isSameAs(email);
        assertThat(result.options()).hasSize(2);

        assertThat(result.options().get(0)).isInstanceOf(PostOfficeOptionResponse.class);
        PostOfficeOptionResponse first = (PostOfficeOptionResponse) result.options().get(0);
        assertThat(first.id()).isEqualTo("sender");
        assertThat(first.type()).isEqualTo("SENDER");

        assertThat(result.options().get(1)).isInstanceOf(PostOfficeOptionResponse.class);
        PostOfficeOptionResponse second = (PostOfficeOptionResponse) result.options().get(1);
        assertThat(second.id()).isEqualTo("link");
        assertThat(second.type()).isEqualTo("LINK");

        verify(optionParser).parse("post-office-json", LevelType.POST_OFFICE);
        verify(optionParser).parsePostOfficeQuestion("post-office-json");
    }

    @Test
    void toQuestionResponse_whenNoOptions_shouldReturnEmptyOptionsList() {
        Level level = level(LevelType.MULTIPLE_CHOICE);
        Question question = question(104, level, "Tomt spørsmål", "empty-json");

        when(optionParser.parse("empty-json", LevelType.MULTIPLE_CHOICE))
                .thenReturn(List.of());

        QuestionResponse result = gameMapper.toQuestionResponse(question);

        assertThat(result.id()).isEqualTo(104);
        assertThat(result.options()).isEmpty();
        assertThat(result.email()).isNull();

        verify(optionParser).parse("empty-json", LevelType.MULTIPLE_CHOICE);
    }

    private Level level(LevelType type) {
        Level level = new Level();
        level.setId(1);
        level.setTitle("Level");
        level.setDescription("Description");
        level.setLevelType(type);
        level.setOrder(1);
        return level;
    }

    private Question question(int id, Level level, String text, String optionContent) {
        Question question = new Question();
        question.setId(id);
        question.setLevel(level);
        question.setQuestionText(text);
        question.setOptionContent(optionContent);
        return question;
    }
}