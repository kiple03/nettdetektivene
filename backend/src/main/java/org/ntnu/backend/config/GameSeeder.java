package org.ntnu.backend.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Idempotent seeder: safe to run on any DB state.
 *
 * <p>On each startup it:
 * <ol>
 *   <li>Deduplicates stops by name — keeps the lowest-id row, deletes extras with their levels and questions.</li>
 *   <li>Ensures all required stops, levels, questions, and level-note templates exist (upsert by natural key).</li>
 * </ol>
 *
 * <p>Note: {@link LevelNoteTemplate} is now a {@code @OneToOne} with {@link Level},
 * so each level has at most one template.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GameSeeder implements CommandLineRunner {

  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final QuestionRepository questionRepository;
  private final OptionValidator optionValidator;
  private final LevelNoteTemplateRepository levelNoteTemplateRepository;

  /**
   * Executes automatically when the Spring Boot application starts.
   * Checks if the database is already seeded by verifying if any Stops exist.
   * If the database is empty, it triggers the initial seeding process.
   *
   * @param args Command line arguments passed to the application.
   */
  @Override
  @Transactional
  public void run(String... args) {
    log.info("GameSeeder: deduplicating and ensuring seed data...");
    deduplicateStops();
    deduplicateLevels();
    deduplicateLevelNoteTemplates();
    seedNews();
    seedPasswords();
    seedPhoto();
    seedMarkedsplassen();
    log.info("GameSeeder: done");
  }

  /**
   * For each stop name that has more than one row, keep the lowest-id row and
   * delete the duplicates along with their levels and questions.
   */
  private void deduplicateStops() {
    List<Stop> allStops = stopRepository.findAll();
    allStops.stream()
        .map(Stop::getName)
        .distinct()
        .forEach(name -> {
          List<Stop> duplicates = stopRepository.findAllByNameOrderByIdAsc(name);
          if (duplicates.size() <= 1) return;
          for (Stop dup : duplicates.subList(1, duplicates.size())) {
            log.info("GameSeeder: deleting duplicate stop '{}' id={}", name, dup.getId());
            deleteStop(dup);
          }
        });
  }

  private void deleteStop(Stop stop) {
    List<Level> levels = levelRepository.findByStopIdOrderByIdAsc(stop.getId());
    for (Level level : levels) {
      deleteLevel(level);
    }
    stopRepository.delete(stop);
  }

  /**
   * For each stop, deduplicate levels by title and keep the lowest-id row.
   */
  private void deduplicateLevels() {
    List<Stop> allStops = stopRepository.findAll();
    for (Stop stop : allStops) {
      List<Level> levels = levelRepository.findByStopIdOrderByIdAsc(stop.getId());
      levels.stream()
          .map(Level::getTitle)
          .distinct()
          .forEach(title -> {
            List<Level> duplicates =
                levelRepository.findAllByStopIdAndTitleOrderByIdAsc(stop.getId(), title);
            if (duplicates.size() <= 1) return;
            for (Level dup : duplicates.subList(1, duplicates.size())) {
              log.info("GameSeeder: deleting duplicate level '{}' id={} for stop '{}'",
                  title, dup.getId(), stop.getName());
              deleteLevel(dup);
            }
          });
    }
  }

  /**
   * Since {@link LevelNoteTemplate} is now {@code @OneToOne} with {@link Level},
   * any level with more than one template row is invalid legacy data.
   * Keep the lowest-id template per level and delete the rest.
   */
  private void deduplicateLevelNoteTemplates() {
    List<Level> allLevels = levelRepository.findAll();
    for (Level level : allLevels) {
      List<LevelNoteTemplate> templates =
          levelNoteTemplateRepository.findAllByLevelIdOrderByIdAsc(level.getId());
      if (templates.size() <= 1) continue;
      for (LevelNoteTemplate dup : templates.subList(1, templates.size())) {
        log.info("GameSeeder: deleting duplicate level note template id={} for level id={}",
            dup.getId(), level.getId());
        levelNoteTemplateRepository.delete(dup);
      }
    }
  }

  private void deleteLevel(Level level) {
    // Templates must go before the level itself (FK on level_id is NOT NULL).
    levelNoteTemplateRepository.deleteAll(levelNoteTemplateRepository
        .findAllByLevelIdOrderByIdAsc(level.getId()));

    List<Question> questions = questionRepository.findByLevelIdOrderByIdAsc(level.getId());
    questionRepository.deleteAll(questions);
    levelRepository.delete(level);
  }

  private void seedNews() {
    Stop stop = stopRepository.findByName("Nyhetskvartalet").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Nyhetskvartalet");
      s.setDisplayOrder(1);
      return stopRepository.save(s);
    });

    Level level1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Ekte eller falskt? 1")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Ekte eller falskt? 1");
          l.setDescription("Kan du se forskjell på ekte og falske nyheter? Les overskriftene og velg den du tror er ekte.");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(level1,
        "Hvilken NRK-artikkel er ekte?",
        List.of(
            new MultipleChoiceOption(1, "Norge mangler en plan for matberedskap - løsningen er 5000 nye 'Jon Almaas-er'", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News1-1.png", true, "Den andre artikelen innholder skrivefeil, journalist uten etternavn og merkelig dato og tid."),
            new MultipleChoiceOption(2, "Jon Almaas er den aller beste", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News1-2.png", false, "Denne artikkelen innholder skrivefeil, journalist uten etternavn og merkelig dato og tid.")
        )
    );

    ensureQuestion(level1,
        "Hvilken adressa-artikkel er ekte?",
        List.of(
            new MultipleChoiceOption(1, "EKSTRA penger til flere ansate i barnehagen: -Jeg er jublende glad", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News2-1.png", false, "I denne artikelen blir det brukt store bokstavene og Ukjent stortingspolitker"),
            new MultipleChoiceOption(2, "Ekstra penger til flere ansate i barnehagen: -Jeg er jublende glad", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News2-2.png", true, "Oppdaget du de store bokstavene og den Ukjente stortingspolitikeren?")
        )
    );

    ensureLevelNoteTemplate(level1,
        NoteCategory.NYHETER,
        "Sjekk alltid om overskriften virker troverdig og se etter rare skrivefeil eller manglende navn."
    );


    Level level2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Ekte eller falskt? 2")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Ekte eller falskt? 2");
          l.setDescription("Kan du se forskjell på ekte og falske nyheter? Les overskriftene og velg den du tror er ekte.");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(2);
          return levelRepository.save(l);
        });

    ensureQuestion(level2,
        "Hvilken nyhetssak fra Bergens tidene er ekte?",
        List.of(
            new MultipleChoiceOption(1, "Artikkel 1", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News3-1.png", true, "Den andre atrikkelen inneholder en ukjent mann og 2600-tallet!"),
            new MultipleChoiceOption(2, "Artikkel 2", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News3-2.png", false, "Denne artikkelen innholder en ukjent mann og 2600-tallet")
        )
    );

    ensureQuestion(level2,
        "Hvilken BT-artikkel er ekte?",
        List.of(
            new MultipleChoiceOption(1, "Artikkel 1", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News4-1.png", false, "Hvor er etternavnet til næringsminsteren? Nåer er klokken 09:71?"),
            new MultipleChoiceOption(2, "Artikkel 2", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News4-2.png", true, "Flott! Du oppdaget at næringsministeren mangler etternavn og at det var noe rart med klokkeslettet?")
        )
    );

    Level level3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Ekte eller falskt? 3")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Ekte eller falskt? 3");
          l.setDescription("Kan du se forskjell på ekte og falske nyheter? Les overskriftene og velg den du tror er ekte.");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(3);
          return levelRepository.save(l);
        });

    ensureQuestion(level3,
        "Hvilken adressa-artikkel er ekte?",
        List.of(
            new MultipleChoiceOption(1, "Har solgt over 40 000 biletter", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News5-1.png", true, "Det stemmer! Det er mer logisk riktig å lage artikkel på at 40 000 biletter er solgt! I tilegg fant du kanskje det manglende etternavnet i bildeteksten?"),
            new MultipleChoiceOption(2, "Har solgt over 20 biletter", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News5-2.png", false, "Fornavn brukes sjeldent alene i nyhetsartikler og 20 billetter solgt er ikke logisk å lage artikkel på.")
        )
    );

    ensureQuestion(level3,
        "Hvilken NRK-nyhetssak er ekte?",
        List.of(
            new MultipleChoiceOption(1, "Artikkel 1", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News6-1.png", true, "Veldig bra! Her stemte alderen overens, det var ingen store bokstaver for å skape frykt og begge journalistene hadde etternavn."),
            new MultipleChoiceOption(2, "Artikkel 2", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/News6-2.png", false, "Her stemte ikke alderen overens, det er brukt store bokstaver og en av journalistene mangler etternavn")
        )
    );

    ensureLevelNoteTemplate(level2,
        NoteCategory.NYHETER,
        "Dummy notat for nivå 2 — sjekk datoer, klokkeslett og om journalisten har fullt navn."
    );

    // ... level3 definition and questions ...

    ensureLevelNoteTemplate(level3,
        NoteCategory.NYHETER,
        "Dummy notat for nivå 3 — vurder om tallene og alderen i artikkelen gir logisk mening."
    );
  }

  private void seedPhoto() {
    Stop stop = stopRepository.findByName("Fotografen").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Fotografen");
      s.setDisplayOrder(2);
      return stopRepository.save(s);
    });

    Level level = levelRepository.findByStopIdAndTitle(stop.getId(), "Ekte, KI eller manipulert?").orElseGet(() -> {
      Level l = new Level();
      l.setTitle("Ekte, KI eller manipulert?");
      l.setDescription("Se nøye på bildet og velg riktig svar.");
      l.setLevelType(LevelType.PICTURE_QUIZ);
      l.setStop(stop);
      return levelRepository.save(l);
    });

    ensurePictureQuizQuestion(
            level,
            "Hvilke bilder er AI-generert?",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/SantaClauseReal.png", false,
                            "Bildet er ikke ekte. Det har tegn på KI-generering."),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/SantaClauseAi.png", true,
                            "Riktig. Ansiktstrekkene og detaljene i bildet tyder på at det er KI-generert."),
                    new PictureQuizOption(3, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/aiFake.png", true,
                            "Riktig! Dette bildet er KI generert"),
                    new PictureQuizOption(4, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/img_2.png", false,
                            "Bildet er ikke ekte. Det har tegn på KI-generering.")
            )
    );

    ensurePictureQuizQuestion(
            level,
            "Hvilket bilde er IKKE ekte?",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/robben.png", false,
                            "Feil! Bildet er Helt ekte!"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/FootballFakePic.png", true,
                            "Riktig. Bildet er manipulert, altså endret fra et ekte utgangspunkt."),
                    new PictureQuizOption(3, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/FootballReal.png", false,
                            "Feil! Bildet er Helt ekte!"),
                    new PictureQuizOption(4, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/crouch.png", false,
                            "Feil! Bildet er Helt ekte!")
            )
    );

    ensurePictureQuizQuestion(
            level,
            "Marker bildet genrert av AI",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/manAI.png", true,
                            "Riktig. Rare detaljer som glinsende, ren og myk hud er lette identifikatorer"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/img_1.png", false,
                            "Feil!, Dette er et helt ekte bilde!"),
                    new PictureQuizOption(3, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/beetle.png", false,
                            "Feil!, Dette er et helt ekte bilde!"),
                    new PictureQuizOption(4, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/john.png", false,
                            "Feil!, Dette er et helt ekte bilde!")
            )
    );

    Level level2 = levelRepository
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Ekte, KI eller manipulert? 2")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Ekte, KI eller manipulert? 2");
              l.setDescription("Se nøye på bildet og velg riktig svar.");
              l.setLevelType(LevelType.PICTURE_QUIZ);
              l.setStop(stop);
              l.setOrder(2);
              return levelRepository.save(l);
          });

    ensurePictureQuizQuestion(
            level2,
            "Hvilket bilde er av ekte natur?",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/fakeAI.png", false,
                            "Feil!, ingen natur ser slik ut!"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/norsknatur.png", true,
                            "Helt riktig!, dette er helt nydelig norsk natur!")
            )
    );

    ensurePictureQuizQuestion(
            level2,
            "Hva er riktig påstand?",
            List.of(
                    new PictureQuizOption(1, "Bildet er ikke ekte fordi mange rare ting skjer samtidig," +
                            " og sånt finnes ikke i virkeligheten.",
                            null, true, "Helt riktig!"),
                    new PictureQuizOption(2, "Bildet er ekte fordi jeg har sett det", null, false, "Helt feil!"),
                    new PictureQuizOption(3, "Jeg vet ikke", null, false, "Helt feil!"),
                    new PictureQuizOption(4, "Bildet er ekte fordi alt ser helt naturlig ut", null, false, "Helt feil!")
            )
    );

    ensurePictureQuizQuestion(
            level2,
            "Hvilke reklamer er AI-generert?",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/aireklame.png",
                            true, "Helt riktig!"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/ektereklame.png",
                            false, "Feil!, Denne reklamen er ikke AI generert."),
                    new PictureQuizOption(3, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/ektereklame2.png",
                            false, "Feil!, Denne reklamen er ikke AI generert."),
                    new PictureQuizOption(4, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/aireklameigjen.png",
                            true, "Helt riktig!")

            )
    );

    Level level3 = levelRepository
            .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Ekte, KI eller manipulert? 3")
            .orElseGet(() -> {
                Level l = new Level();
                l.setTitle("Ekte, KI eller manipulert? 3");
                l.setDescription("Se nøye på bildet og velg riktig svar.");
                l.setLevelType(LevelType.PICTURE_QUIZ);
                l.setStop(stop);
                l.setOrder(3);
                return levelRepository.save(l);
            });

    ensurePictureQuizQuestion(
            level3,
            "Hvilket bilde er AI?",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/zebra.jpg",
                            false, "Feil! Det er et ekte zebra"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/zebraFake.png",
                            true, "Riktig! dette bildet er faktisk generert av AI")
            )
    );

    ensurePictureQuizQuestion(
            level3,
            "Hvilket bilde er ekte",
            List.of(
                    new PictureQuizOption(1, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/fotballai.png",
                            false, "Feil! Det er et AI generert bilde!"),
                    new PictureQuizOption(2, null, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/fotballnotai.png",
                            true, "Riktig! dette bildet er faktisk ikke generert av AI")
            )
    );
  }

  private void ensurePictureQuizQuestion(Level level, String questionText, List<PictureQuizOption> options) {
    boolean exists = questionRepository
            .findByLevelIdAndQuestionText(level.getId(), questionText)
            .isPresent();
    if (exists) return;

    List<Option> optionList = new ArrayList<>();
    optionList.addAll(options);

    optionValidator.validate(optionList, level.getLevelType());

    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writeOptions(optionList));
    questionRepository.save(question);
  }

  private void ensureQuestion(Level level, String questionText, List<Option> options) {
    boolean exists = questionRepository
        .findByLevelIdAndQuestionText(level.getId(), questionText)
        .isPresent();
    if (exists) return;

    List<Option> optionList = new ArrayList<>();
    optionList.addAll(options);

    optionValidator.validate(optionList, level.getLevelType());

    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writeOptions(options));
    questionRepository.save(question);
  }

  private String writeOptions(List<? extends Option> options) {
    try {
      return new ObjectMapper().writeValueAsString(options);
    } catch (JacksonException e) {
      log.error("Error while writing options json", e);
      throw new IllegalStateException("Failed to serialize options", e);
    }
  }

  /**
   * Upserts the single template for a level. Since {@link LevelNoteTemplate} is
   * {@code @OneToOne} with {@link Level}, we either create one if none exists,
   * or update the existing one if category/content drifted.
   */
  private void ensureLevelNoteTemplate(Level level, NoteCategory category, String content) {
    levelNoteTemplateRepository.findByLevelId(level.getId())
        .ifPresentOrElse(
            existing -> {
              if (existing.getCategory() != category || !existing.getContent().equals(content)) {
                existing.setCategory(category);
                existing.setContent(content);
                levelNoteTemplateRepository.save(existing);
              }
            },
            () -> {
              LevelNoteTemplate template = LevelNoteTemplate.builder()
                  .level(level)
                  .category(category)
                  .content(content)
                  .build();
              levelNoteTemplateRepository.save(template);
            }
        );
  }

  private void seedPasswords() {
      Stop stop = stopRepository.findByName("Passordbanken").orElseGet(() -> {
          Stop s = new Stop();
          s.setName("Passordbanken");
          s.setDisplayOrder(4);
          return stopRepository.save(s);
      });

      // ---------- LEVEL 1 — password strength (new type) ----------
      Level l1 = levelRepository
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Lag et sterkt passord")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Lag et sterkt passord");
              l.setDescription("Skriv inn et passord. Systemet sier fra hvor sterkt det er.");
              l.setLevelType(LevelType.PASSWORD_STRENGTH);
              l.setStop(stop);
              l.setOrder(1);
              return levelRepository.save(l);
          });

      ensureQuestion(l1,
          "Lag et passord på minst 8 tegn med tall og stor bokstav.",
          List.of(new PasswordStrengthOption(
              1, 8, true, true, false, false,
              "Bra! Passordet oppfyller kravene.",
              "Passordet ditt er ikke sterkt nok."
          ))
      );

      ensureQuestion(l1,
          "Nå skjerper vi kravene: minst 12 tegn, med tall, stor bokstav OG symbol.",
          List.of(new PasswordStrengthOption(
              1, 12, true, true, false, true,
              "Utmerket — dette er et robust passord!",
              "Passordet ditt er ikke sterkt nok."
          ))
      );

      ensureLevelNoteTemplate(l1, NoteCategory.PASSORD,
          "Lange passord med blanding av store/små bokstaver, tall og symboler er vanskelige å gjette.");

      // ---------- LEVEL 2 — multiple choice: what makes a good password ----------
      Level l2 = levelRepository
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Hva er et godt passord?")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Hva er et godt passord?");
              l.setDescription("Velg alternativet som beskriver et godt passord.");
              l.setLevelType(LevelType.MULTIPLE_CHOICE);
              l.setStop(stop);
              l.setOrder(2);
              return levelRepository.save(l);
          });

      ensureQuestion(l2,
          "Hvilket av passordene er tryggest?",
          List.of(
              new MultipleChoiceOption(1, "fotball2010",          "", false,
                  "Et ord fra ordboka pluss et årstall er lett å gjette."),
              new MultipleChoiceOption(2, "Kj0leskap!Blå_Sky12",  "", true,
                  "Riktig! Langt, variert og uten ordbokord."),
              new MultipleChoiceOption(3, "12345678",             "", false,
                  "Et av verdens vanligste passord — gjettes på sekunder."),
              new MultipleChoiceOption(4, "passord",              "", false,
                  "Direkte ordet 'passord' er det første noen prøver.")
          )
      );

      ensureQuestion(l2,
          "Hvilken regel er VIKTIGST for et sterkt passord?",
          List.of(
              new MultipleChoiceOption(1, "At det er kort og lett å huske",                "", false,
                  "Korte passord gir angripere et lite søkerom."),
              new MultipleChoiceOption(2, "At det inneholder kjælenavnet ditt",            "", false,
                  "Personlig info er lett å finne i sosiale medier."),
              new MultipleChoiceOption(3, "At det er langt og variert, og bare brukes ett sted", "",
  true,
                  "Riktig — lengde + unikhet per konto er det som betyr mest."),
              new MultipleChoiceOption(4, "At det bare består av tall",                    "", false,
                  "Uten bokstaver og symboler blir søkerommet for lite.")
          )
      );

      ensureLevelNoteTemplate(l2, NoteCategory.PASSORD,
          "Bruk unike passord per tjeneste, og vurder en passordbehandler.");

      // ---------- LEVEL 3 — multiple choice: what to do on a leak ----------
      Level l3 = levelRepository
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Passordlekkasje — hva gjør du?")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Passordlekkasje — hva gjør du?");
              l.setDescription("Velg riktig handling hvis passordet ditt er lekket.");
              l.setLevelType(LevelType.MULTIPLE_CHOICE);
              l.setStop(stop);
              l.setOrder(3);
              return levelRepository.save(l);
          });

      ensureQuestion(l3,
          "Du ser at passordet ditt er lekket på haveibeenpwned. Hva gjør du først?",
          List.of(
              new MultipleChoiceOption(1, "Ignorerer det, det går nok over",         "", false,
                  "Lekkede passord testes av angripere i løpet av få timer."),
              new MultipleChoiceOption(2, "Bytter passordet på den lekkede tjenesten og alle steder det gjenbrukes", "", true,
                  "Riktig — stopp gjenbruk først."),
              new MultipleChoiceOption(3, "Skriver passordet på en post-it",         "", false,
                  "Dette løser ikke lekkasjen og øker fysisk risiko."),
              new MultipleChoiceOption(4, "Sender det til en venn for råd",          "", false,
                  "Du sprer eksponeringen uten å løse problemet.")
          )
      );

      ensureQuestion(l3,
          "Hvilket tiltak reduserer skaden av fremtidige lekkasjer mest?",
          List.of(
              new MultipleChoiceOption(1, "Aldri bruke nettet",                      "", false,
                  "Urealistisk og unødvendig."),
              new MultipleChoiceOption(2, "Slå på to-faktor-autentisering",          "", true,
                  "Riktig — selv om passordet lekkes, mangler angriperen andre faktor."),
              new MultipleChoiceOption(3, "Bruke samme passord overalt for enkelhet","", false,
                  "Motsatt — én lekkasje kompromitterer da alle kontoer."),
              new MultipleChoiceOption(4, "Skrive alle passord i en tekstfil på skrivebordet", "",
  false,
                  "Malware leser slike filer trivielt.")
          )
      );

      ensureLevelNoteTemplate(l3, NoteCategory.PASSORD,
          "Bytt passord og skru på 2FA så fort du oppdager lekkasje.");
  }

  private void seedMarkedsplassen() {
    Stop stop = stopRepository.findByName("Markedsplassen").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Markedsplassen");
      s.setDisplayOrder(5);
      return stopRepository.save(s);
    });

    Level l1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Er denne nettbutikken trygg?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Er denne nettbutikken trygg?");
          l.setDescription("Du har funnet noen nettbutikker. Se nøye på tilbudene og vurder om du bør handle der.");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(l1,
        "Du finner en nettbutikk som heter 'SuperBillig-Shop.xyz'. De har 90% rabatt på ALT, ingen kontaktinformasjon, og nettsiden ser ut som den ble laget i går. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Kjøpe – dette er et godt tilbud!", null, false,
                "Denne nettbutikken har mange varseltegn. 90% rabatt på alt er nesten alltid for godt til å være sant."),
            new MultipleChoiceOption(2, "Undersøke mer først", null, false,
                "Det er bra å være forsiktig, men denne butikken har så mange røde flagg at det er best å holde seg unna."),
            new MultipleChoiceOption(3, "Ikke handle – dette virker utrygt", null, true,
                "Riktig! Butikken har 90% rabatt på alt, ingen kontaktinfo og en merkelig nettadresse. Dette er typiske tegn på svindel.")
        )
    );

    ensureQuestion(l1,
        "En nettbutikk selger populære sko til halv pris. Siden ser profesjonell ut, men alle anmeldelsene er 5 stjerner og skrevet på samme dag. Du finner ikke noe info om hvem som eier butikken. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Kjøpe – det ser fint ut", null, false,
                "Selv om siden ser pen ut, bør du være forsiktig. Alle anmeldelsene er skrevet på samme dag, og det finnes ingen informasjon om hvem som driver butikken."),
            new MultipleChoiceOption(2, "Undersøke mer først", null, true,
                "Riktig! Butikken ser grei ut ved første øyekast, men det er noen ting som bør sjekkes nærmere – som anmeldelsene og manglende info om selskapet."),
            new MultipleChoiceOption(3, "Ikke handle – dette virker utrygt", null, false,
                "Det er lurt å være forsiktig, men her er det bedre å undersøke mer først. Noen av tegnene kan ha en forklaring.")
        )
    );

    ensureQuestion(l1,
        "Du finner en nettbutikk med norsk organisasjonsnummer, telefonnummer og e-post på kontaktsiden. Nettadressen starter med hengelås og slutter på '.no'. Anmeldelsene ser ekte ut med både gode og dårlige tilbakemeldinger. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Kjøpe – dette ser trygt ut", null, true,
                "Riktig! Denne butikken har organisasjonsnummer, kontaktinfo, sikker betaling og ekte anmeldelser. Dette er tegn på en trygg nettbutikk."),
            new MultipleChoiceOption(2, "Undersøke mer først", null, false,
                "Det er alltid lurt å være forsiktig, men denne butikken viser alle tegnene på at den er trygg og seriøs."),
            new MultipleChoiceOption(3, "Ikke handle – jeg stoler ikke på nettbutikker", null, false,
                "Denne butikken er faktisk trygg! Den har kontaktinfo, organisasjonsnummer, hengelås i nettleseren og ekte anmeldelser. Det er viktig å kunne kjenne igjen trygge butikker også.")
        )
    );

    ensureLevelNoteTemplate(l1, NoteCategory.NETT,
        "Sjekk alltid kontaktinfo, nettadresse og anmeldelser før du handler på nett. Hvis noe virker for godt til å være sant, er det ofte det.");
    Level l2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Hva er mistenkelig?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Hva er mistenkelig?");
          l.setDescription("Noe er galt med disse nettbutikkene. Kan du finne ut hva som er mistenkelig?");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(2);
          return levelRepository.save(l);
        });

    ensureQuestion(l2,
        "En nettbutikk selger jakker til 50 kr. Nettadressen er 'www.billige-jakker-norge.ru'. Hva er mest mistenkelig?",
        List.of(
            new MultipleChoiceOption(1, "Prisen er lav", null, false,
                "Lave priser kan være mistenkelig, men det finnes ekte tilbud. Det mest mistenkelige her er nettadressen."),
            new MultipleChoiceOption(2, "Nettadressen ender på .ru", null, true,
                "Riktig! En norsk nettbutikk bør ha en norsk nettadresse. '.ru' er russisk, og det er rart for en butikk som later som den er norsk."),
            new MultipleChoiceOption(3, "De selger jakker", null, false,
                "Det er ingenting galt med å selge jakker. Se nøyere på nettadressen!")
        )
    );

    ensureQuestion(l2,
        "Du finner en nettbutikk med mange gode anmeldelser, men alle er skrevet av 'Bruker123' med litt forskjellig tekst. Hva er mistenkelig?",
        List.of(
            new MultipleChoiceOption(1, "Butikken har anmeldelser", null, false,
                "Det er bra at en butikk har anmeldelser! Problemet er at de alle ser ut til å komme fra samme person."),
            new MultipleChoiceOption(2, "Alle anmeldelsene er fra samme bruker", null, true,
                "Riktig! Ekte anmeldelser kommer fra mange forskjellige personer. Når alle er fra samme bruker, er de sannsynligvis falske."),
            new MultipleChoiceOption(3, "Teksten er litt forskjellig", null, false,
                "Det er normalt at anmeldelser har forskjellig tekst. Problemet er hvem som har skrevet dem.")
        )
    );

    ensureQuestion(l2,
        "En nettbutikk tilbyr kun betaling med gavekort eller kryptovaluta. Hva er mistenkelig?",
        List.of(
            new MultipleChoiceOption(1, "At butikken finnes på nett", null, false,
                "Det er helt vanlig å handle på nett. Det mistenkelige er betalingsmåten."),
            new MultipleChoiceOption(2, "Betalingsmåten – du kan ikke spore pengene", null, true,
                "Riktig! Trygge nettbutikker tilbyr vanlige betalingsmetoder som Vipps, kort eller faktura. Gavekort og krypto kan ikke spores, så du får aldri pengene tilbake."),
            new MultipleChoiceOption(3, "At de tar betaling i det hele tatt", null, false,
                "Alle butikker tar betaling. Det mistenkelige er hvilke betalingsmåter de tilbyr.")
        )
    );

    ensureLevelNoteTemplate(l2, NoteCategory.NETT,
        "Sjekk nettadressen, hvem som har skrevet anmeldelsene, og om betalingsmåtene er trygge.");


    Level l3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nettbutikk-eksperten")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nettbutikk-eksperten");
          l.setDescription("Gå gjennom nettbutikken og vurder hvert element.");
          l.setLevelType(LevelType.CLASSIFY_ITEMS);
          l.setStop(stop);
          l.setOrder(3);
          return levelRepository.save(l);
        });

    ensureQuestion(l3,
        "Du har funnet nettbutikken 'ElektroTilbud'. Hva tror du om hvert element?",
        List.of(
            new ClassifyItemsOption("url", "Nettadressen er www.elektrotilbud.xyz",
                List.of("Ekte norsk butikk", "Utenlandsk side", "Ukjent domene"),
                "Ukjent domene",
                ".xyz er ikke et norsk domene. Norske butikker bruker vanligvis .no — et ukjent domene betyr at du bør være ekstra forsiktig."),
            new ClassifyItemsOption("reviews", "Alle 200 anmeldelsene er 5 stjerner og lagt inn samme dag",
                List.of("Ekte anmeldelser", "Kjøpte anmeldelser", "Ansatte som har skrevet"),
                "Kjøpte anmeldelser",
                "200 perfekte anmeldelser på én dag er nesten helt sikkert kjøpt. Ekte anmeldelser kommer over tid fra forskjellige folk."),
            new ClassifyItemsOption("contact", "Kontaktsiden har telefonnummer, e-post og besøksadresse",
                List.of("Vanlig for nettbutikker", "Uvanlig mye info", "Sikkert falsk info"),
                "Vanlig for nettbutikker",
                "Seriøse butikker har alltid kontaktinfo. Du kan ringe eller besøke dem hvis noe går galt."),
            new ClassifyItemsOption("payment", "Betaling kun via kryptovaluta",
                List.of("Moderne betalingsløsning", "Uvanlig for norske butikker", "Helt normalt"),
                "Uvanlig for norske butikker",
                "Norske nettbutikker tilbyr alltid Vipps, kort eller faktura. Kun krypto betyr at pengene ikke kan spores tilbake.")
        )
    );

    ensureQuestion(l3,
        "Du ser en annonse for 'GamerShop' på sosiale medier. Hva tenker du om hvert element?",
        List.of(
            new ClassifyItemsOption("ad", "Annonsen sier 'KUN I DAG: 90% på alt!'",
                List.of("Vanlig tilbud", "Lokketilbud", "Sesongkampanje"),
                "Lokketilbud",
                "90% rabatt på alt er urealistisk. Slike tilbud skaper hastverk så du handler uten å tenke."),
            new ClassifyItemsOption("spelling", "Nettsiden har skrivefeil som 'bestil nå' og 'levaringtid'",
                List.of("Norsk dialekt", "Dårlig oversettelse", "Vanlige skrivefeil"),
                "Dårlig oversettelse",
                "Mange slike feil tyder på at siden er automatisk oversatt fra et annet språk — vanlig for svindelbutikker."),
            new ClassifyItemsOption("price", "En PlayStation 5 koster vanlig butikkpris",
                List.of("For dyrt", "Rettferdig pris", "Mistenkelig billig"),
                "Rettferdig pris",
                "Normal pris er faktisk et godt tegn! Svindelbutikker lokker med urealistisk lave priser."),
            new ClassifyItemsOption("return", "Det finnes ingen informasjon om retur eller angrerett",
                List.of("Normalt for nettbutikker", "Bryter norsk lov", "Betyr gratis retur"),
                "Bryter norsk lov",
                "Norske nettbutikker er pålagt å tilby 14 dagers angrerett. Manglende info om dette er et alvorlig rødt flagg.")
        )
    );

    ensureLevelNoteTemplate(l3, NoteCategory.NETT,
        "Vurder hele nettbutikken samlet: nettadresse, priser, anmeldelser, betaling og returrett. Én trygg detalj betyr ikke at alt er trygt.");


  }
}
