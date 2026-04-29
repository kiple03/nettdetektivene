package org.ntnu.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.PostOfficeQuestion;
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
    seedPost();
    seedPhoto();
    seedPasswords();
    seedMarkedsplassen();
    seedSocial();
    seedBoos();
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
          // keep duplicates.get(0), delete the rest
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

  /**
   * Seed news stop, levels and questions.
   */
  private void seedNews() {
    Stop stop = stopRepository.findByName("Nyhetskvartalet").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Nyhetskvartalet");
      s.setDisplayOrder(1);
      return stopRepository.save(s);
    });

    /* level 1 */
    Level level1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 1: Ekte eller falskt?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 1: Ekte eller falskt?");
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

    /* notes level 1 */
    ensureLevelNoteTemplate(level1,
        NoteCategory.NYHETER,
        "Sjekk alltid om overskriften virker troverdig og se etter rare skrivefeil eller manglende navn."
    );

    /* level 2 */
    Level level2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 2: Ekte eller falskt?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 2: Ekte eller falskt?");
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

    /* notes level 2 */
    ensureLevelNoteTemplate(level2,
        NoteCategory.NYHETER,
        "Sjekk dato og tidspunkt. Normen i nyhetsartikler er at fullt navn eller bare etteravn blir brukt. "
    );

    /* level 3 */
    Level level3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 3: Ekte eller falskt?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 3: Ekte eller falskt?");
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

    /* notes level 3*/
    ensureLevelNoteTemplate(level3,
        NoteCategory.NYHETER,
        "Sjekk om artikkelen er logisk riktig og om de bruker STORE bokstaver for å skape frykt eller andre følelser."
    );
  }

  /**
   * Seeds photo stop, level and questions.
   */
  private void seedPhoto() {
    Stop stop = stopRepository.findByName("Fotografen").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Fotografen");
      s.setDisplayOrder(2);
      return stopRepository.save(s);
    });

    /* level 1 */
    Level level = levelRepository.findByStopIdAndTitle(stop.getId(), "Nivå 1: Ekte, KI eller manipulert?").orElseGet(() -> {
      Level l = new Level();
      l.setTitle("Nivå 1: Ekte, KI eller manipulert?");
      l.setDescription("Se nøye på bildet og velg riktig svar.");
      l.setLevelType(LevelType.PICTURE_QUIZ);
      l.setOrder(1);
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

    /* notes level 1 */
    ensureLevelNoteTemplate(level,
        NoteCategory.BILDER,
        "Vær oppmerksom på urealistiske objekter og for glinsende, ren og myk hud."
    );

    /* level 2 */
    Level level2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 2: Ekte, KI eller manipulert?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 2: Ekte, KI eller manipulert?");
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

    /* notes level 2 */
    ensureLevelNoteTemplate(level2,
        NoteCategory.BILDER,
        "Sjekk om bedriften eksisterer."
    );

    Level level3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 3: Ekte, KI eller manipulert?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 3: Ekte, KI eller manipulert?");
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

    /* notes level 3 */
    ensureLevelNoteTemplate(level3,
        NoteCategory.BILDER,
        "Kunstig intelligens har blitt veldig god på generering av bilder. Spør en voksen hvis du er usikker!"
    );
  }

  /**
   * Seeds post stop, levels and questions.
   */
  private void seedPost() {
    Stop stop3 = stopRepository.findByName("Postkontoret").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Postkontoret");
      s.setDisplayOrder(3);
      return stopRepository.save(s);
    });

    /* level 1 */
    Level postLevel1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Nivå 1: Finn det mistenklige i mailen!")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 1: Finn det mistenklige i mailen! 1");
          l.setDescription("Kan du hva som er mistenkelig i mailene?");
          l.setLevelType(LevelType.POST_OFFICE);
          l.setStop(stop3);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(postLevel1,
        "Hva er mistenklig med eposten fra Apple?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Apple",
                "apple123@apple.com",
                null,
                "DU har vunnet en Iphone 17 PRO!",
                "2026-02-07T13:22:00Z",
                List.of(
                    "Gratulerer du har vunnet en helt ny.",
                    "Iphone 17 PRO.",
                    "Klikk på linker under NÅ!",
                    "",
                    "Ikke glem det før det er for sent"
                ),
                List.of(
                    new PostOfficeQuestion.Link("signup", "Klikk her", "http://fånå.no")
                ),
                null,
                "Med vennlig hilsen \n Apple"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", true,  "Avsenderadressen er ikke den ekte Apple eposten."),
                new PostOfficeOption("signup",      "link",        true,  "Du ser ikke hvilken nettside lenken tar deg."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        true, "Innholdet virker ikke ekte."),
                new PostOfficeOption("subject",     "subject",     true, "Emnefeltet ser ut som lureri."),
                new PostOfficeOption("signature",   "signature",   false, "Signaturen ser normalt ut.")
            ),
            List.of("senderEmail", "link", "body", "subject"),
            "Spør en voksen"
        )
    );

    ensureQuestion(postLevel1,
        "Hva er mistenkelig med denne e-posten fra leos lekeland?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Leo's lekeland",
                "hello@leoslekeland.no",
                "hello@leoslekeland.no",
                "Velkommen til Leo's lekeland!",
                "2026-04-24T13:22:00Z",
                List.of(
                    "Hei!",
                    "Takk for din bestilling på Leos lekeland."
                ),
                null,
                List.of(
                    new PostOfficeQuestion.Image("promo", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/leos.jpg", "Logo leo's lekeland")
                ),
                "Hilsen Leos lekeland"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", false,  "Avsenderadressen er fra en kjent organisasjon."),
                new PostOfficeOption("signup",      "link",        false,  "Ingen lenke."),
                new PostOfficeOption("promo",       "image",       true, "Bildet er ikke Leos lekeland sin logo."),
                new PostOfficeOption("body",        "body",        false, "Innholdet ser normalt ut."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   false, "Signaturen ser normalt ut.")
            ),
            List.of("image"),
            "Spør en voksen"
        )
    );

    /* notes level 1 */
    ensureLevelNoteTemplate(postLevel1,
        NoteCategory.EPOST,
        "Sjekk om avsender-epost er ekte, innholdet er lokkende, lenken er ekte og om logoen er ekte."
    );

    /* level 2 */
    Level postLevel2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Nivå 2: Finn det mistenklige i mailen!")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 2: Finn det mistenklige i mailen!");
          l.setDescription("Kan du hva som er mistenkelig i mailene?");
          l.setLevelType(LevelType.POST_OFFICE);
          l.setStop(stop3);
          l.setOrder(2);
          return levelRepository.save(l);
        });

    ensureQuestion(postLevel2,
        "Hva er mistenkelig med denne e-posten fra Skoleklubben?",
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
                    new PostOfficeQuestion.Image("promo", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/lego.png", "Barn som bygger LEGO")
                ),
                "Hilsen Skoleklubben"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", true,  "Avsenderadressen er ikke fra en kjent organisasjon."),
                new PostOfficeOption("signup",      "link",        true,  "Lenken peker til et ukjent nettsted og du vet ikke hvor du havner."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        false, "Innholdet ser normalt ut."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   false, "Signaturen ser normalt ut.")
            ),
            List.of("senderEmail", "signup"),
            "Spør en voksen"
        )
    );

    ensureQuestion(postLevel2,
        "Hva er mistenklig med eposten fra Roblox?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Roblox",
                "no-reply@roblox-support.com",
                null,
                "Roblox Email Reset",
                "2026-02-07T13:22:00Z",
                List.of(
                    "Dear Roblox user,",
                    "",
                    "We noticed that you have change the email address for your Lucky account from l******@gmail.com to",
                    "l*******@hotmail.com. Just in case you really didn't mean to change it, or you think someone else changed",
                    "it by mistake, then we need you to click this link",
                    "https://www.roblox.com/login/revertAccount",
                    "to change the email back. You will also need to enter a new password. That way we will know for sure that",
                    "your account is secure and safe. \n",
                    "If you are happy with your new email address on Roblox your don't have to do anything! It's already set up.\n",
                    "Please do not reply to this message if you have any question pleas email info@roblox.com",
                    ""
                ),
                null,
                null,
                "This message was generated by https://roblox.com/"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", true,  "Den ekte Roblox-mailen er @roblox.com"),
                new PostOfficeOption("signup",      "link",        false,  "Ingen klikkbar lenke."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        false, "Innholdet ser normalt ut, lenken er ekte og ikke en knapp."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   true, "Signaturen til ekte Roblox bruker roblox.com.")
            ),
            List.of("senderEmail", "signature"),
            "Spør en voksen"
        )
    );

    /* notes level 2 */
    ensureLevelNoteTemplate(postLevel2,
        NoteCategory.EPOST,
        "Sjekk om avsender-epost er ekte, lenken er ekte og signaturen."
    );

    Level postLevel3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Nivå 3: Finn det mistenklige i mailen!")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 3: Finn det mistenklige i mailen!");
          l.setDescription("Kan du hva som er mistenkelig i mailene?");
          l.setLevelType(LevelType.POST_OFFICE);
          l.setStop(stop3);
          l.setOrder(3);
          return levelRepository.save(l);
        });

    ensureQuestion(postLevel3,
        "Hva er mistenkelig med denne e-posten fra leos lekeland?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Leo's lekeland",
                "hello@leoslekeland.no",
                "hello@leoslekeland.no",
                "Velkommen til Leo's lekeland!",
                "2026-04-24T13:22:00Z",
                List.of(
                    "Hei!",
                    "Takk for din bestilling på Leos lekeland."
                ),
                null,
                List.of(
                    new PostOfficeQuestion.Image("promo", "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/leos2.jpg", "Logo leo's lekeland")
                ),
                "Hilsen Leos lekkeeland"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", false,  "Avsenderadressen er fra en kjent organisasjon."),
                new PostOfficeOption("signup",      "link",        false,  "Ingen lenke."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        false, "Innholdet ser normalt ut."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   true, "Signaturen er ikke riktig.")
            ),
            List.of("signature"),
            "Spør en voksen"
        )
    );

    ensureQuestion(postLevel3,
        "Hva er mistenklig med eposten fra Roblox?",
        new PostOfficeQuestion(
            new PostOfficeQuestion.Email(
                "Roblox",
                "no-reply@roblox.com",
                null,
                "Roblox Email Reset",
                "2026-02-07T13:22:00Z",
                List.of(
                    "Dear Roblox user,",
                    "",
                    "We noticed that you have change the email address for your Lucky account from l******@gmail.com to",
                    "l*******@hotmail.com. Just in case you really didn't mean to change it, or you think someone else changed",
                    "it by mistake, please reset via the button under",
                    "If you are happy with your new email address on Roblox your don't have to do anything! It's already set up.\n",
                    "Please do not reply to this message if you have any question pleas email email.info@roblox.com",
                    ""
                ),
                List.of(
                    new PostOfficeQuestion.Link("reset", "Reset", "http://roblox/FakeReset")
                ),
                null,
                "This message was generated by roblox.com"
            ),
            List.of(
                new PostOfficeOption("senderEmail", "senderEmail", false,  "Avsendermailen er ekte"),
                new PostOfficeOption("reset",      "link",        true,  "Lenke med knapp uten lenkeadresse er mistenkelig."),
                new PostOfficeOption("promo",       "image",       false, "Bildet ser normalt ut."),
                new PostOfficeOption("body",        "body",        true, "Den inneholder en uekte mail og vil at du skal trykke på en knapp."),
                new PostOfficeOption("subject",     "subject",     false, "Emnefeltet ser normalt ut."),
                new PostOfficeOption("signature",   "signature",   true, "Signaturen er ekte.")
            ),
            List.of("senderEmail", "signature"),
            "Spør en voksen"
        )
    );
    /* notes level 3 */
    ensureLevelNoteTemplate(postLevel3,
        NoteCategory.EPOST,
        "Sjekk alle små detaljer som mailadresser og skrivefeil."
    );
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

  private String writePostOfficeQuestion(PostOfficeQuestion postOfficeQuestion) {
    try {
      return new ObjectMapper().writeValueAsString(postOfficeQuestion);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize PostOfficeQuestion", e);
    }
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
    question.setDefault(true);
    question.setClassroomId(null);
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
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 1: Lag et sterkt passord")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Nivå 1: Lag et sterkt passord");
              l.setDescription("Skriv inn et passord. Systemet sier fra hvor sterkt det er.");
              l.setLevelType(LevelType.PASSWORD_STRENGTH);
              l.setStop(stop);
              l.setOrder(1);
              return levelRepository.save(l);
          });

      ensureQuestion(l1,
          "Lag et passord på minst 8 tegn med tall og stor bokstav.",
          List.of(new PasswordStrengthOption(
              1, 8, true, true, false, false, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/passord1.png",
              "Bra! Passordet oppfyller kravene.",
              "Passordet ditt er ikke sterkt nok."
          ))
      );

      ensureQuestion(l1,
          "Nå skjerper vi kravene: minst 12 tegn, med tall, stor bokstav OG symbol.",
          List.of(new PasswordStrengthOption(
              1, 12, true, true, false, true, "http://s3-egsm5ptz727wk9ikygsuit6i.65.21.148.53.sslip.io/nettdetektivene-bilder/passord2.png",
              "Utmerket — dette er et robust passord!",
              "Passordet ditt er ikke sterkt nok."
          ))
      );

      ensureLevelNoteTemplate(l1, NoteCategory.PASSORD,
          "Lange passord med blanding av store/små bokstaver, tall og symboler er vanskelige å gjette.");

      // ---------- LEVEL 2 — multiple choice: what makes a good password ----------
      Level l2 = levelRepository
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 2: Hva er et godt passord?")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Nivå 2: Hva er et godt passord?");
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
          .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 3: Passordlekkasje — hva gjør du?")
          .orElseGet(() -> {
              Level l = new Level();
              l.setTitle("Nivå 3: Passordlekkasje — hva gjør du?");
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
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 1: Er denne nettbutikken trygg?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 1: Er denne nettbutikken trygg?");
          l.setDescription("Du har funnet noen nettbutikker. Se nøye på tilbudene og vurder om du bør handle der.");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(l1,
        "Du finner en nettbutikk som heter 'SuperBillig-Shop.xyz'. De har 90% rabatt på alle varer. Du finner ingen kontaktinformasjon, og nettsiden ser ut som den ble laget i går. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Kjøper, dette er et godt tilbud!", null, false,
                "90% er mistenkelig, men jeg kjøper en vare jeg har lyst på. Nettsiden er tross alt nosrk."),
            new MultipleChoiceOption(2, "Undersøke mer først", null, false,
                "Det er bra å være forsiktig, men denne butikken har så mange røde flagg at det er best å holde seg unna."),
            new MultipleChoiceOption(3, "Ikke handle, dette virker utrygt", null, true,
                "Riktig! Butikken har 90% rabatt på alt, ingen kontaktinfo og en merkelig nettadresse. Dette er typiske tegn på svindel.")
        )
    );

    ensureQuestion(l1,
        "En nettbutikk selger populære sko til halv pris. Siden ser profesjonell ut, men alle anmeldelsene er 5 stjerner og skrevet på samme dag. Du finner ikke noe info om hvem som eier butikken. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Undersøke mer først", null, true,
                "Riktig! Butikken ser grei ut ved første øyekast, men det er noen ting som bør sjekkes nærmere, som anmeldelsene og manglende info om selskapet."),
            new MultipleChoiceOption(2, "Kjøpe, nettsiden ser profejsonell ut", null, false,
                "Selv om siden ser pen ut, bør du være forsiktig. Alle anmeldelsene er skrevet på samme dag, og det finnes ingen informasjon om hvem som driver butikken."),
            new MultipleChoiceOption(3, "Dette virker utrygt, her skal jeg ikke handle", null, false,
                "Det er lurt å være forsiktig, men her er det bedre å undersøke mer først. Noen av tegnene kan ha en forklaring.")
        )
    );

    ensureQuestion(l1,
        "Du finner en nettbutikk med telefonnummer og e-post på kontaktsiden. Nettadressen har hengelås og slutter på '.no'. Anmeldelsene gir både gode og dårlige tilbakemeldinger. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Kjøpe, dette ser trygt ut", null, true,
                "Riktig! Denne butikken har kontaktinfo, sikker nettadresse og ekte anmeldelser. Dette er tegn på en trygg nettbutikk."),
            new MultipleChoiceOption(2, "Undersøke mer først", null, false,
                "Det er alltid lurt å være forsiktig, men denne butikken viser alle tegnene på at den er trygg og seriøs."),
            new MultipleChoiceOption(3, "Ikke handle, jeg stoler ikke på nettbutikker", null, false,
                "Denne butikken er faktisk trygg! Den har kontaktinfo, hengelås i nettleseren og ekte anmeldelser. Det er viktig å kunne kjenne igjen trygge butikker også.")
        )
    );

    ensureLevelNoteTemplate(l1, NoteCategory.NETT,
        "Sjekk alltid kontaktinfo, nettadresse og anmeldelser før du handler på nett. Hvis noe virker for godt til å være sant, er det ofte det.");
    Level l2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 2: Hva er mistenkelig?")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 2: Hva er mistenkelig?");
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
            new MultipleChoiceOption(1, "Butikken har med anmeldelser", null, false,
                "Det er bra at en butikk har anmeldelser! Problemet er at de alle ser ut til å komme fra samme person."),
            new MultipleChoiceOption(2, "Alle anmeldelsene er fra samme bruker", null, true,
                "Riktig! Ekte anmeldelser kommer fra mange forskjellige personer. Når alle er fra samme bruker, er de sannsynligvis falske."),
            new MultipleChoiceOption(3, "Teksten er litt forskjellig", null, false,
                "Det er normalt at anmeldelser har forskjellig tekst. Problemet er hvem som har skrevet dem.")
        )
    );

    ensureQuestion(l2,
        "En nettbutikk lar deg bare betale med Roblox-gavekort eller PlayStation-koder. Hva er mistenkelig?",
        List.of(
            new MultipleChoiceOption(1, "At butikken finnes på nett", null, false,
                "Det er helt vanlig å handle på nett. Det mistenkelige er betalingsmåten."),
            new MultipleChoiceOption(2, "Betalingsmåten, spillkort er ikke ekte betaling overalt", null, true,
                "Riktig! Trygge nettbutikker lar deg betale med Vipps eller kort. Hvis noen ber om spillkort som betaling, er det nesten alltid svindel."),
            new MultipleChoiceOption(3, "At de tar betaling i det hele tatt", null, false,
                "Alle butikker tar betaling. Det mistenkelige er at de ber om spillkort i stedet for vanlige betalingsmåter som Vipps eller kort.")
        )
    );

    ensureLevelNoteTemplate(l2, NoteCategory.NETT,
        "Sjekk nettadressen, hvem som har skrevet anmeldelsene, og om betalingsmåtene er trygge.");


    Level l3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Nivå 3: Nettbutikk-eksperten")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Nivå 3: Nettbutikk-eksperten");
          l.setDescription("Gå gjennom nettbutikken og vurder hvert element.");
          l.setLevelType(LevelType.CLASSIFY_ITEMS);
          l.setStop(stop);
          l.setOrder(3);
          return levelRepository.save(l);
        });

    ensureQuestion(l3,
        "Du har funnet nettbutikken 'ElektroTilbud'. Hva tenker du om følgende observasjoner?",
        List.of(
            new ClassifyItemsOption("url", "Nettadressen er www.elektrotilbud.fun",
                List.of("Ekte norsk nettbutikk", "Rart at den ikke slutter på .no", "Ser helt vanlig ut"),
                "Rart at den ikke slutter på .no",
                "Norske nettbutikker bruker vanligvis .no. Når adressen slutter på noe annet bør du være ekstra forsiktig."),
            new ClassifyItemsOption("reviews", "Alle 2000 anmeldelsene er 5 stjerner og lagt inn i løpet av et par dager",
                List.of("Ekte anmeldelser", "Kjøpte anmeldelser", "Må ha utrolig høy kvalitet"),
                "Kjøpte anmeldelser",
                "2000 perfekte anmeldelser på noen dager er nesten helt sikkert kjøpt. Ekte anmeldelser kommer over tid fra forskjellige folk."),
            new ClassifyItemsOption("contact", "Kontaktsiden har telefonnummer, e-post og adresse",
                List.of("Vanlig for nettbutikker", "Uvanlig mye info", "Ikke viktig"),
                "Vanlig for nettbutikker",
                "Seriøse butikker har alltid kontaktinfo. Du kan ringe dem hvis noe går galt."),
            new ClassifyItemsOption("payment", "Du kan bare betale med gavekort",
                List.of("Vanlig betalingsmåte", "Rart — du kan ikke klage etterpå", "Helt greit"),
                "Rart — du kan ikke klage etterpå",
                "Trygge nettbutikker lar deg betale med Vipps eller kort. Med gavekort får du aldri pengene tilbake hvis noe er galt.")
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
            new ClassifyItemsOption("price", "En PlayStation 5 koster det samme som i butikken",
                List.of("For dyrt", "Rettferdig pris", "Mistenkelig billig"),
                "Rettferdig pris",
                "Normal pris er faktisk et godt tegn! Svindelbutikker lokker med urealistisk lave priser.")
        )
    );

    ensureLevelNoteTemplate(l3, NoteCategory.NETT,
        "Vurder hele nettbutikken samlet: nettadresse, priser, anmeldelser, betaling og returrett. Én trygg detalj betyr ikke at alt er trygt.");

  }

  /** Seed social stop, levels and questions **/
  private void seedSocial() {
    Stop stop = stopRepository.findByName("Den sosiale møteplassen").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Den sosiale møteplassen");
      s.setDisplayOrder(6);
      return stopRepository.save(s);
    });

    Level l1 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Tenk før du deler")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Tenk før du deler");
          l.setDescription("Du ser innlegg på sosiale medier. Hva ville du gjort?");
          l.setLevelType(LevelType.MULTIPLE_CHOICE);
          l.setStop(stop);
          l.setOrder(1);
          return levelRepository.save(l);
        });

    ensureQuestion(l1,
        "Du ser en TikTok som sier: 'Hvis du ikke deler denne videoen, vil noe fælt skje om 7 dager!' Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Deler videoen med en gang. Jeg vil ikke oppleve noe fælt!", null, false,
                "Ingen video vil gjøre at do opplever noe fælt! Slike meldinger er laget for å lure deg til å dele."),
            new MultipleChoiceOption(2, "Ignorerer det, dette er bare tull", null, true,
                "Riktig! Ingen video vil gjøre at do opplever noe fælt. Slike meldinger prøver å skremme deg til å dele."),
            new MultipleChoiceOption(3, "Deler den bare til bestevennen min", null, false,
                "Da sprer du meldingen videre! Den er bare tull, og det er trygt å ignorere den.")
        )
    );

    ensureQuestion(l1,
        "Noen i klassen din legger ut et morsomt, men flaut bilde av en annen elev uten å spørre. Mange ler og deler det. Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Deler det ikke, og sier ifra til en voksen", null, true,
                "Riktig! Alle har rett til å bestemme over sine egne bilder. Det er modig å si ifra."),
            new MultipleChoiceOption(2, "Ler og deler det videre, det er jo morsomt!", null, false,
                "Det kan føles morsomt, men den som er på bildet synes kanskje ikke det. Man skal alltid spørre først."),
            new MultipleChoiceOption(3, "Sier ingenting, men deler det heller ikke", null, false,
                "Bra at du ikke deler! Men det er enda bedre å si ifra til en voksen, så den som er på bildet får hjelp.")
        )
    );

    ensureQuestion(l1,
        "En du ikke kjenner sender deg en melding på Snapchat: 'Hei! Vil du bli venner? Send bilde av deg selv!' Hva gjør du?",
        List.of(
            new MultipleChoiceOption(1, "Sender et bilde, jeg vil gjerne ha nye venner!", null, false,
                "Du vet ikke hvem denne personen egentlig er. Send aldri bilder til noen du ikke kjenner."),
            new MultipleChoiceOption(2, "Svarer ikke og forteller en voksen om det", null, true,
                "Riktig! Du skal aldri sende bilder til noen du ikke kjenner. Snakk med en voksen du stoler på."),
            new MultipleChoiceOption(3, "Spør hvem de er først, og sender bilde hvis de virker greie", null, false,
                "Selv om noen virker greie, vet du ikke hvem de egentlig er. Ikke send bilder til fremmede.")
        )
    );

    ensureLevelNoteTemplate(l1, NoteCategory.SOSIALE,
        "Tenk deg om før du deler. Hvis noen prøver å skremme eller presse deg til å dele noe videre, er det et tegn på at du bør stoppe opp.");

    // ---------- LEVEL 2 — PICTURE_QUIZ: Finn advarselstegnene ----------
    Level l2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Finn advarselstegnene")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Finn advarselstegnene");
          l.setDescription("Se på innleggene og velg alle advarselstegnene du finner.");
          l.setLevelType(LevelType.PICTURE_QUIZ);
          l.setStop(stop);
          l.setOrder(2);
          return levelRepository.save(l);
        });

    ensureQuestion(l2,
        "Et innlegg sier: 'BREAKING: Skoler i Norge stenges for ALLTID! 😱🚨 Del til alle du kjenner!!' Hva er advarselstegn? Velg alle som passer.",
        List.of(
            new PictureQuizOption(1, "Store bokstaver og mange utropstegn", null, true,
                "Riktig! Store bokstaver og utropstegn brukes for å skape panikk."),
            new PictureQuizOption(2, "Mange emojier for å skape frykt", null, true,
                "Riktig! Skremmende emojier brukes for å forsterke følelser og få deg til å reagere uten å tenke."),
            new PictureQuizOption(3, "Oppfordring til å dele raskt", null, true,
                "Riktig! 'Del til alle' er et press-triks for å spre informasjon før noen rekker å sjekke den."),
            new PictureQuizOption(4, "Innlegget handler om skole", null, false,
                "At et innlegg handler om skole er ikke et advarselstegn i seg selv. Det er måten det er skrevet på som er problemet.")
        )
    );

    ensureQuestion(l2,
        "Du ser denne meldingen i en gruppechat: '😱😱 IKKE spis godteriet fra Kiwi!! En gutt ble SKIKKELIG SYK!! 😱😱 SEND TIL ALLE!!' Hva er advarselstegn? Velg alle som passer.",
        List.of(
            new PictureQuizOption(1, "Mange skumle emojier", null, true,
                "Riktig! Skremmende emojier brukes for å gjøre deg redd uten grunn."),
            new PictureQuizOption(2, "Store bokstaver og utropstegn", null, true,
                "Riktig! STORE BOKSTAVER brukes for å lage panikk."),
            new PictureQuizOption(3, "'Send til alle' – press om å dele", null, true,
                "Riktig! Når noen sier du MÅ dele, prøver de å spre meldingen uten at noen sjekker om det er sant."),
            new PictureQuizOption(4, "At det handler om godteri", null, false,
                "At det handler om godteri er ikke et advarselstegn. Det er måten meldingen er skrevet på som er problemet.")
        )
    );

    ensureLevelNoteTemplate(l2, NoteCategory.SOSIALE,
        "Advarselstegn: store bokstaver, hastverk, ukjente kilder, konspirasjonsspråk og emosjonelt press.");

    // ---------- LEVEL 3 — CLASSIFY_ITEMS: Vurder innlegget ----------
    Level l3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop.getId(), "Vurder innlegget")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Vurder innlegget");
          l.setDescription("Les hvert element fra et innlegg på sosiale medier og vurder hva det er.");
          l.setLevelType(LevelType.CLASSIFY_ITEMS);
          l.setStop(stop);
          l.setOrder(3);
          return levelRepository.save(l);
        });

    ensureQuestion(l3,
        "Noen på skolen deler et innlegg i gruppechatten. Vurder hvert element.",
        List.of(
            new ClassifyItemsOption("message", "Meldingen: 'Hvis du er en EKTE venn, deler du dette videre!'",
                List.of("Vennlig melding", "Press fra venner", "God oppfordring"),
                "Press fra venner",
                "Når noen sier du er en 'ekte venn' bare hvis du gjør noe, prøver de å presse deg."),
            new ClassifyItemsOption("emoji", "Det er brukt masse 😡 og 💔 emojier",
                List.of("Bare pynt", "Brukt for å skape sterke følelser", "Viser at avsenderen er lei seg"),
                "Brukt for å skape sterke følelser",
                "Sinte og triste emojier brukes for å få deg til å føle noe sterkt, så du handler uten å tenke."),
            new ClassifyItemsOption("claim", "'Alle i klassen har allerede delt det!'",
                List.of("Sikkert sant", "Press – du må gjøre som alle andre", "Bra å vite"),
                "Press – du må gjøre som alle andre",
                "Bare fordi 'alle gjør det' betyr ikke at du må. Det er ofte ikke sant heller."),
            new ClassifyItemsOption("action", "Du kan velge å dele, vente, eller spørre en voksen",
                List.of("Unødvendig", "Lurt å tenke seg om", "Feigt"),
                "Lurt å tenke seg om",
                "Det er alltid lurt å stoppe opp og tenke før du deler noe videre. Du kan alltid spørre en voksen.")
        )
    );

    ensureQuestion(l3,
        "Du ser et innlegg på Instagram fra en du følger. Vurder hvert element.",
        List.of(
            new ClassifyItemsOption("photo", "Et bilde av en kjendis med teksten 'Gi bort gratis iPhone til alle som deler!'",
                List.of("Ekte konkurranse", "For godt til å være sant", "Reklame"),
                "For godt til å være sant",
                "Gratis ting til alle som deler er nesten alltid svindel. Ekte konkurranser fungerer ikke sånn."),
            new ClassifyItemsOption("comments", "Alle kommentarene sier 'Håper jeg vinner!! 🤞' og ingen stiller spørsmål",
                List.of("Bevis på at det er ekte", "Folk som også er lurt", "Hyggelig stemning"),
                "Folk som også er lurt",
                "At mange tror på det gjør det ikke sant. Alle i kommentarfeltet kan ha blitt lurt av det samme."),
            new ClassifyItemsOption("link", "Lenken i innlegget går til en side du aldri har hørt om",
                List.of("Helt normalt", "Bør sjekkes nøye", "Sikkert trygt"),
                "Bør sjekkes nøye",
                "Ukjente lenker kan føre til svindelsider. Klikk aldri på lenker du ikke kjenner igjen.")
        )
    );

    ensureLevelNoteTemplate(l3, NoteCategory.SOSIALE,
        "Vurder alltid kilden, se etter følelsesmanipulasjon, og husk at popularitet ikke er det samme som sannhet.");
  }


  private void seedBoos() {
    stopRepository.findByName("Datasenteret").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Datasenteret");
      s.setDisplayOrder(7);
      return stopRepository.save(s);
    });
  }
}
