package org.ntnu.backend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.game.model.*;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.util.OptionValidator;
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
 *   <li>Ensures all required stops, levels, and questions exist (upsert by natural key).</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GameSeeder implements CommandLineRunner {

  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final QuestionRepository questionRepository;
  private final OptionValidator optionValidator;

  /**
   * Executes automatically when the Spring Boot application starts.
   * Checks if the database is already seeded by verifying if any Stops exist.
   * If the database is empty, it triggers the initial seeding process.
   *
   * @param args Command line arguments passed to the application.
   */
  @Override
  public void run(String... args) {
    log.info("GameSeeder: deduplicating and ensuring seed data...");
    deduplicateStops();
    seedNews();
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
      List<Question> questions = questionRepository.findByLevelIdOrderByIdAsc(level.getId());
      questionRepository.deleteAll(questions);
      levelRepository.delete(level);
    }
    stopRepository.delete(stop);
  }

  private void seedNews() {
    Stop stop = stopRepository.findByName("Nyhetskvartalet").orElseGet(() -> {
      Stop s = new Stop();
      s.setName("Nyhetskvartalet");
      s.setDisplayOrder(1);
      return stopRepository.save(s);
    });

    Level level = levelRepository.findByStopIdAndTitle(stop.getId(), "Ekte eller falskt?").orElseGet(() -> {
      Level l = new Level();
      l.setTitle("Ekte eller falskt?");
      l.setDescription("Kan du se forskjell på ekte og falske nyheter? Les overskriftene og velg den du tror er ekte.");
      l.setLevelType(LevelType.MULTIPLE_CHOICE);
      l.setStop(stop);
      return levelRepository.save(l);
    });

    ensureQuestion(level,
        "Hvilken av disse overskriftene er fra en ekte nyhetssak?",
        List.of(
            new MultipleChoiceOption(1, "SJOKKERENDE: Forskere finner at mobilstråling gjør deg smartere over natten!", "placeholder.jpg", false, "Denne bruker store bokstaver og overdriver. Ekte nyheter er mer nøkterne."),
            new MultipleChoiceOption(2, "Ny rapport viser at norske barn bruker mer tid på skjerm enn før", "placeholder.jpg", true, "Denne er saklig og kommer fra en troverdig kilde med redaktøransvar."),
            new MultipleChoiceOption(3, "Lærere HATER denne appen – den gjør leksene for deg!!", "placeholder.jpg", false, "Clickbait-overskrift. Seriøse nyheter bruker ikke 'HATER' og utropstegn.")
        )
    );

    ensureQuestion(level,
        "En venn sender deg en nyhet på Snapchat. Hva bør du gjøre først?",
        List.of(
            new MultipleChoiceOption(1, "Dele den videre med en gang så alle får vite det", "placeholder.jpg", false, "Aldri del noe videre uten å sjekke om det er sant først."),
            new MultipleChoiceOption(2, "Sjekke om nyheten finnes på NRK, TV2 eller andre kjente medier", "placeholder.jpg", true, "Riktig! Sjekk alltid om troverdige medier har skrevet om det samme."),
            new MultipleChoiceOption(3, "Tro på det fordi vennen din er til å stole på", "placeholder.jpg", false, "Selv venner kan dele falske nyheter uten å vite det.")
        )
    );

    ensureQuestion(level,
        "Hvilket tegn tyder på at en nyhetsartikkel kan være falsk?",
        List.of(
            new MultipleChoiceOption(1, "Artikkelen har dato og navn på journalisten", "placeholder.jpg", false, "Dette er tegn på en seriøs artikkel. Ekte nyheter oppgir alltid forfatter og dato."),
            new MultipleChoiceOption(2, "Artikkelen har mange skrivefeil og rare bilder", "placeholder.jpg", true, "Riktig! Falske nyheter har ofte dårlig språk og manipulerte bilder."),
            new MultipleChoiceOption(3, "Artikkelen handler om noe du ikke visste fra før", "placeholder.jpg", false, "At noe er nytt for deg betyr ikke at det er falskt. Sjekk kilden i stedet.")
        )
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
      l.setDescription("Se nøye på bildet og velg om det er ekte, KI-generert eller manipulert.");
      l.setLevelType(LevelType.PICTURE_QUIZ);
      l.setStop(stop);
      return levelRepository.save(l);
    });

    ensurePictureQuestion(
            level,
            "Ser dette bildet ekte ut, eller er det laget med KI?",
            List.of(
                    new PictureQuizOption(1, "Ekte", "/images/photographer/girl-1.png", false,
                            "Bildet er ikke ekte. Det har tegn på KI-generering."),
                    new PictureQuizOption(2, "KI-generert", "/images/photographer/girl-1.png", true,
                            "Riktig. Ansiktstrekkene og detaljene i bildet tyder på at det er KI-generert."),
                    new PictureQuizOption(3, "Manipulert", "/images/photographer/girl-1.png", false,
                            "Bildet ser ikke ut som et ekte foto som bare er redigert. Det ser KI-generert ut.")
            )
    );

    ensurePictureQuestion(
            level,
            "Hvilken kategori passer best for dette bildet?",
            List.of(
                    new PictureQuizOption(1, "Ekte", "/images/photographer/street-1.png", false,
                            "Dette bildet er ikke et vanlig ekte foto."),
                    new PictureQuizOption(2, "KI-generert", "/images/photographer/street-1.png", false,
                            "Det er ikke hovedsakelig KI-generert."),
                    new PictureQuizOption(3, "Manipulert", "/images/photographer/street-1.png", true,
                            "Riktig. Bildet er manipulert, altså endret fra et ekte utgangspunkt.")
            )
    );

    ensurePictureQuestion(
            level,
            "Er dette bildet KI-generert?",
            List.of(
                    new PictureQuizOption(1, "Ja, fordi hendene og detaljene ser unormale ut", "/images/photographer/girl-2.png", true,
                            "Riktig. Rare detaljer som hender, hår, øredobber eller bakgrunn kan være tegn på KI."),
                    new PictureQuizOption(2, "Nei, fordi bildet ser fint og realistisk ut", "/images/photographer/girl-2.png", false,
                            "Selv om et bilde ser realistisk ut, kan det fortsatt være KI-generert.")
            )
    );
  }

  private void ensurePictureQuestion(Level level, String questionText, List<PictureQuizOption> options) {
    boolean exists = questionRepository
            .findByLevelIdAndQuestionText(level.getId(), questionText)
            .isPresent();
    if (exists) return;

    optionValidator.validate(List.copyOf(options), level.getLevelType());

    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writePictureOptions(options));
    questionRepository.save(question);
  }

  private String writePictureOptions(List<PictureQuizOption> options) {
    try {
      return new ObjectMapper().writeValueAsString(options);
    } catch (JacksonException e) {
      log.error("Error while writing picture quiz options json", e);
      throw new IllegalStateException("Failed to serialize picture quiz options", e);
    }
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

  /**
   * Serializes a list of multiple-choice options into a JSON string to be stored in the database.
   *
   * @param options The list of options to be converted to JSON.
   * @return A JSON string representing the provided options.
   * @throws IllegalStateException If the JSON serialization process fails.
   */
  private String writeOptions(List<MultipleChoiceOption> options) {
    try {
      return new ObjectMapper().writeValueAsString(options);
    } catch (JacksonException e) {
      log.error("Error while writing options json", e);
      throw new IllegalStateException("Failed to serialize options", e);
    }
  }
}
