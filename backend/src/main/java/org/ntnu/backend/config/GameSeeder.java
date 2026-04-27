package org.ntnu.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.PostOfficeQuestion;
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

  @Override
  public void run(String... args) {
    log.info("GameSeeder: deduplicating and ensuring seed data...");
    deduplicateStops();
    deduplicateLevels();
    deduplicateLevelNoteTemplates();
    seedNews();
    seedPost();
    log.info("GameSeeder: done");
  }

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

  private void seedPost() {
    Stop stop3 = stopRepository.findByName("Postkontoret").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Postkontoret");
      s.setDisplayOrder(3);
      return stopRepository.save(s);
    });

    Level postLevel1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Finn det mistenklige i mailen! 1")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Finn det mistenklige i mailen! 1");
          l.setDescription("Kan du hva som er mistenkelig i mailene?");
          l.setLevelType(LevelType.POST_OFFICE);
          l.setStop(stop3);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(postLevel1,
        "Hva er mistenkelig med denne e-posten?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Skoleklubben",
                "info@skoleklubb-bygg.no",
                "kontakt@skoleklubb-bygg.no",
                "Invitasjon til byggkonkurranse — vinn LEGO-sett!",
                "2026-04-24T13:22:00Z",
                List.of(
                    "Hei!",
                    "Vi inviterer alle elever til en morsom byggkonkurranse.",
                    "Vinneren får et splitter nytt LEGO-sett og et gavekort på 500 kr.",
                    "Meld deg på via lenken under. Husk å spørre en voksen om hjelp!"
                ),
                List.of(
                    new PostOfficeQuestion.Link("signup", "Meld deg på her", "http://skoleklubb-bygg.no")
                ),
                List.of(
                    new PostOfficeQuestion.Image("promo", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/24/LEGO_logo.svg/250px-LEGO_logo.svg.png", "Barn som bygger LEGO")
                ),
                "Hilsen Skoleklubben"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", true,  "Avsenderadressen er ikke fra en kjent organisasjon."),
                new PostOfficeOption("signup",      "link",        true,  "Lenken peker til et ukjent nettsted."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        false, "Innholdet ser normalt ut."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   false, "Signaturen ser normalt ut.")
            ),
            List.of("senderEmail", "signup"), // correctSuspicious unchanged
            "Spør en voksen"
        )
    );
  }

    private void ensureQuestion(Level level, String questionText, List<MultipleChoiceOption> options) {
    boolean exists = questionRepository
        .findByLevelIdAndQuestionText(level.getId(), questionText)
        .isPresent();
    if (exists) return;

    optionValidator.validate(List.copyOf(options), level.getLevelType());

    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writeOptions(options));
    questionRepository.save(question);
  }

  private void ensureQuestion(Level level, String questionText, PostOfficeQuestion postOfficeQuestion) {
    boolean exists = questionRepository
        .findByLevelIdAndQuestionText(level.getId(), questionText)
        .isPresent();
    if (exists) return;
    optionValidator.validate(List.copyOf(postOfficeQuestion.getOptions()), level.getLevelType());
    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writePostOfficeQuestion(postOfficeQuestion));
    questionRepository.save(question);
  }
  private String writeOptions(List<MultipleChoiceOption> options) {
    try {
      return new ObjectMapper().writeValueAsString(options);
    } catch (JacksonException e) {
      log.error("Error while writing options json", e);
      throw new IllegalStateException("Failed to serialize options", e);
    }
  }

  private String writePostOfficeQuestion(PostOfficeQuestion postOfficeQuestion) {
    try {
      return new ObjectMapper().writeValueAsString(postOfficeQuestion);
    } catch (JacksonException e) {
      log.error("Error while writing post office questions json", e);
      throw new IllegalStateException("Failed to serialize PostOfficeQuestion", e);
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
}