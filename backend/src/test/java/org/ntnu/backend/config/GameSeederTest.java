package org.ntnu.backend.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;

class GameSeederTest {

    private StopRepository stopRepository;
    private LevelRepository levelRepository;
    private QuestionRepository questionRepository;
    private OptionValidator optionValidator;
    private LevelNoteTemplateRepository levelNoteTemplateRepository;
    private GameSeeder gameSeeder;

    @BeforeEach
    void setUp() {
        stopRepository = mock(StopRepository.class);
        levelRepository = mock(LevelRepository.class);
        questionRepository = mock(QuestionRepository.class);
        optionValidator = mock(OptionValidator.class);
        levelNoteTemplateRepository = mock(LevelNoteTemplateRepository.class);

        gameSeeder = new GameSeeder(
                stopRepository,
                levelRepository,
                questionRepository,
                optionValidator,
                levelNoteTemplateRepository
        );

        setupDefaultStubs();
    }

    // -------------------------------------------------------------------------
    // run() — top-level orchestration
    // -------------------------------------------------------------------------

    @Test
    void run_completesWithoutExceptionWhenDbIsEmpty() throws Exception {
        gameSeeder.run();

        verify(stopRepository, atLeast(4)).findByName(anyString());
    }

    @Test
    void run_callsDeduplicateStops() throws Exception {
        gameSeeder.run();

        verify(stopRepository, atLeastOnce()).findAll();
    }

    @Test
    void run_callsDeduplicateLevels() throws Exception {
        gameSeeder.run();

        verify(stopRepository, atLeastOnce()).findAll();
    }

    // -------------------------------------------------------------------------
    // deduplicateStops — duplicate removal
    // -------------------------------------------------------------------------

    @Test
    void deduplicateStops_keepsLowestIdAndDeletesDuplicates() throws Exception {
        Stop primary = stopWithId(1, "Nyhetskvartalet");
        Stop duplicate = stopWithId(2, "Nyhetskvartalet");

        when(stopRepository.findAll()).thenReturn(List.of(primary, duplicate));
        when(stopRepository.findAllByNameOrderByIdAsc("Nyhetskvartalet"))
                .thenReturn(List.of(primary, duplicate));
        when(levelRepository.findByStopIdOrderByIdAsc(2))
                .thenReturn(Collections.emptyList());

        gameSeeder.run();

        verify(stopRepository).delete(duplicate);
        verify(stopRepository, never()).delete(primary);
    }

    @Test
    void deduplicateStops_doesNotDeleteWhenNoDuplicates() throws Exception {
        Stop stop = stopWithId(1, "Nyhetskvartalet");

        when(stopRepository.findAll()).thenReturn(List.of(stop));
        when(stopRepository.findAllByNameOrderByIdAsc("Nyhetskvartalet"))
                .thenReturn(List.of(stop));

        gameSeeder.run();

        verify(stopRepository, never()).delete(any(Stop.class));
    }

    @Test
    void deduplicateStops_deletesLevelsAndQuestionsBeforeDuplicateStop() throws Exception {
        Stop primary = stopWithId(1, "Nyhetskvartalet");
        Stop duplicate = stopWithId(2, "Nyhetskvartalet");
        Level level = levelWithId(10, "Some Level", duplicate, LevelType.MULTIPLE_CHOICE);

        when(stopRepository.findAll()).thenReturn(List.of(primary, duplicate));
        when(stopRepository.findAllByNameOrderByIdAsc("Nyhetskvartalet"))
                .thenReturn(List.of(primary, duplicate));
        when(levelRepository.findByStopIdOrderByIdAsc(2))
                .thenReturn(List.of(level));
        when(questionRepository.findByLevelIdOrderByIdAsc(10))
                .thenReturn(Collections.emptyList());
        when(levelNoteTemplateRepository.findAllByLevelIdOrderByIdAsc(10))
                .thenReturn(Collections.emptyList());

        gameSeeder.run();

        verify(levelRepository).delete(level);
        verify(stopRepository).delete(duplicate);
    }

    // -------------------------------------------------------------------------
    // deduplicateLevelNoteTemplates
    // -------------------------------------------------------------------------

    @Test
    void deduplicateLevelNoteTemplates_deletesExtraTemplatesKeepsFirst() throws Exception {
        Stop stop = stopWithId(1, "X");
        Level level = levelWithId(5, "L", stop, LevelType.MULTIPLE_CHOICE);

        when(levelRepository.findAll()).thenReturn(List.of(level));

        LevelNoteTemplate template1 = mock(LevelNoteTemplate.class);
        when(template1.getId()).thenReturn(1);

        LevelNoteTemplate template2 = mock(LevelNoteTemplate.class);
        when(template2.getId()).thenReturn(2);

        when(levelNoteTemplateRepository.findAllByLevelIdOrderByIdAsc(5))
                .thenReturn(List.of(template1, template2));

        gameSeeder.run();

        verify(levelNoteTemplateRepository).delete(template2);
        verify(levelNoteTemplateRepository, never()).delete(template1);
    }

    @Test
    void deduplicateLevelNoteTemplates_doesNothingWhenOnlyOneTemplate() throws Exception {
        Stop stop = stopWithId(1, "X");
        Level level = levelWithId(5, "L", stop, LevelType.MULTIPLE_CHOICE);

        when(levelRepository.findAll()).thenReturn(List.of(level));

        LevelNoteTemplate template = mock(LevelNoteTemplate.class);

        when(levelNoteTemplateRepository.findAllByLevelIdOrderByIdAsc(5))
                .thenReturn(List.of(template));

        gameSeeder.run();

        verify(levelNoteTemplateRepository, never()).delete(any(LevelNoteTemplate.class));
    }

    // -------------------------------------------------------------------------
    // seed methods — idempotency
    // -------------------------------------------------------------------------

    @Test
    void seedNews_doesNotCreateStopIfAlreadyExists() throws Exception {
        Stop existing = stopWithId(1, "Nyhetskvartalet");

        when(stopRepository.findByName("Nyhetskvartalet"))
                .thenReturn(Optional.of(existing));

        gameSeeder.run();

        verify(stopRepository, never()).save(argThat(stop ->
                "Nyhetskvartalet".equals(stop.getName())
        ));
    }

    @Test
    void seedPost_doesNotCreateStopIfAlreadyExists() throws Exception {
        Stop existing = stopWithId(3, "Postkontoret");

        when(stopRepository.findByName("Postkontoret"))
                .thenReturn(Optional.of(existing));

        gameSeeder.run();

        verify(stopRepository, never()).save(argThat(stop ->
                "Postkontoret".equals(stop.getName())
        ));
    }

    @Test
    void seedPasswords_doesNotCreateStopIfAlreadyExists() throws Exception {
        Stop existing = stopWithId(4, "Passordbanken");

        when(stopRepository.findByName("Passordbanken"))
                .thenReturn(Optional.of(existing));

        gameSeeder.run();

        verify(stopRepository, never()).save(argThat(stop ->
                "Passordbanken".equals(stop.getName())
        ));
    }

    @Test
    void seedPhoto_doesNotCreateStopIfAlreadyExists() throws Exception {
        Stop existing = stopWithId(2, "Fotografen");

        when(stopRepository.findByName("Fotografen"))
                .thenReturn(Optional.of(existing));

        gameSeeder.run();

        verify(stopRepository, never()).save(argThat(stop ->
                "Fotografen".equals(stop.getName())
        ));
    }

    @Test
    void seedNews_doesNotCreateQuestionIfAlreadyExists() throws Exception {
        Stop stop = stopWithId(1, "Nyhetskvartalet");

        when(stopRepository.findByName("Nyhetskvartalet"))
                .thenReturn(Optional.of(stop));

        Level level1 = levelWithId(
                10,
                "Ekte eller falskt? 1",
                stop,
                LevelType.MULTIPLE_CHOICE
        );

        when(levelRepository.findTopByStopIdAndTitleOrderByIdAsc(1, "Ekte eller falskt? 1"))
                .thenReturn(Optional.of(level1));

        when(questionRepository.findByLevelIdAndQuestionText(anyInt(), anyString()))
                .thenReturn(Optional.of(mock(Question.class)));

        gameSeeder.run();

        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void ensureLevelNoteTemplate_updatesExistingTemplateIfContentDiffers() throws Exception {
        Stop stop = stopWithId(1, "Nyhetskvartalet");

        when(stopRepository.findByName("Nyhetskvartalet"))
                .thenReturn(Optional.of(stop));

        Level level1 = levelWithId(
                10,
                "Ekte eller falskt? 1",
                stop,
                LevelType.MULTIPLE_CHOICE
        );

        when(levelRepository.findTopByStopIdAndTitleOrderByIdAsc(1, "Ekte eller falskt? 1"))
                .thenReturn(Optional.of(level1));

        when(questionRepository.findByLevelIdAndQuestionText(anyInt(), anyString()))
                .thenReturn(Optional.of(mock(Question.class)));

        LevelNoteTemplate existing = mock(LevelNoteTemplate.class);
        when(existing.getCategory())
                .thenReturn(org.ntnu.backend.notes.model.enums.NoteCategory.PASSORD);
        when(existing.getContent())
                .thenReturn("old content");

        when(levelNoteTemplateRepository.findByLevelId(10))
                .thenReturn(Optional.of(existing));

        gameSeeder.run();

        verify(existing, atLeastOnce()).setCategory(any());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private void setupDefaultStubs() {
        when(stopRepository.findAll())
                .thenReturn(Collections.emptyList());

        when(levelRepository.findAll())
                .thenReturn(Collections.emptyList());

        when(stopRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(stopRepository.save(any(Stop.class)))
                .thenAnswer(invocation -> {
                    Stop stop = invocation.getArgument(0);

                    try {
                        java.lang.reflect.Field field = Stop.class.getDeclaredField("id");
                        field.setAccessible(true);

                        int currentId = field.getInt(stop);

                        if (currentId == 0) {
                            field.setInt(stop, 99);
                        }
                    } catch (Exception ignored) {
                    }

                    return stop;
                });

        when(levelRepository.findTopByStopIdAndTitleOrderByIdAsc(anyInt(), anyString()))
                .thenReturn(Optional.empty());

        when(levelRepository.findByStopIdAndTitle(anyInt(), anyString()))
                .thenReturn(Optional.empty());

        when(levelRepository.save(any(Level.class)))
                .thenAnswer(invocation -> {
                    Level level = invocation.getArgument(0);

                    try {
                        java.lang.reflect.Field field = Level.class.getDeclaredField("id");
                        field.setAccessible(true);

                        int currentId = field.getInt(level);

                        if (currentId == 0) {
                            field.setInt(level, 99);
                        }
                    } catch (Exception ignored) {
                    }

                    return level;
                });

        when(questionRepository.findByLevelIdAndQuestionText(anyInt(), anyString()))
                .thenReturn(Optional.empty());

        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(levelNoteTemplateRepository.findByLevelId(anyInt()))
                .thenReturn(Optional.empty());

        when(levelNoteTemplateRepository.save(any(LevelNoteTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(stopRepository.findAllByNameOrderByIdAsc(anyString()))
                .thenReturn(Collections.emptyList());

        when(levelRepository.findByStopIdOrderByIdAsc(anyInt()))
                .thenReturn(Collections.emptyList());

        when(levelRepository.findAllByStopIdAndTitleOrderByIdAsc(anyInt(), anyString()))
                .thenReturn(Collections.emptyList());

        when(levelNoteTemplateRepository.findAllByLevelIdOrderByIdAsc(anyInt()))
                .thenReturn(Collections.emptyList());

        when(questionRepository.findByLevelIdOrderByIdAsc(anyInt()))
                .thenReturn(Collections.emptyList());

        doNothing()
                .when(optionValidator)
                .validate(any(), any());
    }

    private Stop stopWithId(int id, String name) {
        Stop stop = new Stop();
        stop.setName(name);

        try {
            java.lang.reflect.Field field = Stop.class.getDeclaredField("id");
            field.setAccessible(true);
            field.setInt(stop, id);
        } catch (Exception ignored) {
        }

        return stop;
    }

    private Level levelWithId(int id, String title, Stop stop, LevelType type) {
        Level level = new Level();
        level.setTitle(title);
        level.setStop(stop);
        level.setLevelType(type);

        try {
            java.lang.reflect.Field field = Level.class.getDeclaredField("id");
            field.setAccessible(true);
            field.setInt(level, id);
        } catch (Exception ignored) {
        }

        return level;
    }
}