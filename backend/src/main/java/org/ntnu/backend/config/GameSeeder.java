package org.ntnu.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    seedPasswords();
    seedPasswords();
    seedPhoto();
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

    Level postLevel2 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Finn det mistenklige i mailen! 2")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Finn det mistenklige i mailen! 2");
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

    Level postLevel3 = levelRepository
        .findTopByStopIdAndTitleOrderByIdAsc(stop3.getId(), "Finn det mistenklige i mailen! 3")
        .orElseGet(() -> {
          Level l = new Level();
          l.setTitle("Finn det mistenklige i mailen! 3");
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

    ensureQuestion(postLevel2,
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
}