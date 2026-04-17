package org.ntnu.backend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.service.OptionParser;
import org.ntnu.backend.game.service.OptionValidator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameSeeder implements CommandLineRunner {

  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final QuestionRepository questionRepository;
  private final OptionValidator optionValidator;

  @Override
  public void run(String... args) {
    if (stopRepository.count() > 0) {
      log.info("Database already seeded, skipping");
      return;
    }

    log.info("Seeding database with initial game data...");
    seedNews();
    log.info("Seeding complete");
  }

  private void seedNews() {
    Stop stop = new Stop();
    stop.setName("Nyhetskvartalet");
    stop = stopRepository.save(stop);

    Level level = new Level();
    level.setTitle("Ekte eller falskt?");
    level.setDescription("Kan du se forskjell på ekte og falske nyheter? Les overskriftene og velg den du tror er ekte.");
    level.setLevelType(LevelType.MULTIPLE_CHOICE);
    level.setStop(stop);
    level = levelRepository.save(level);

    seedQuestion(level,
        "Hvilken av disse overskriftene er fra en ekte nyhetssak?",
        List.of(
            new MultipleChoiceOption(
                1,
                "SJOKKERENDE: Forskere finner at mobilstråling gjør deg smartere over natten!",
                "placeholder.jpg",
                false,
                "Denne bruker store bokstaver og overdriver. Ekte nyheter er mer nøkterne."
            ),
            new MultipleChoiceOption(
                2,
                "Ny rapport viser at norske barn bruker mer tid på skjerm enn før",
                "placeholder.jpg",
                true,
                "Denne er saklig og kommer fra en troverdig kilde med redaktøransvar."
            ),
            new MultipleChoiceOption(
                3,
                "Lærere HATER denne appen – den gjør leksene for deg!!",
                "placeholder.jpg",
                false,
                "Clickbait-overskrift. Seriøse nyheter bruker ikke 'HATER' og utropstegn."
            )
        )
    );

    seedQuestion(level,
        "En venn sender deg en nyhet på Snapchat. Hva bør du gjøre først?",
        List.of(
            new MultipleChoiceOption(
                1,
                "Dele den videre med en gang så alle får vite det",
                "placeholder.jpg",
                false,
                "Aldri del noe videre uten å sjekke om det er sant først."
            ),
            new MultipleChoiceOption(
                2,
                "Sjekke om nyheten finnes på NRK, TV2 eller andre kjente medier",
                "placeholder.jpg",
                true,
                "Riktig! Sjekk alltid om troverdige medier har skrevet om det samme."
            ),
            new MultipleChoiceOption(
                3,
                "Tro på det fordi vennen din er til å stole på",
                "placeholder.jpg",
                false,
                "Selv venner kan dele falske nyheter uten å vite det."
            )
        )
    );

    seedQuestion(level,
        "Hvilket tegn tyder på at en nyhetsartikkel kan være falsk?",
        List.of(
            new MultipleChoiceOption(
                1,
                "Artikkelen har dato og navn på journalisten",
                "placeholder.jpg",
                false,
                "Dette er tegn på en seriøs artikkel. Ekte nyheter oppgir alltid forfatter og dato."
            ),
            new MultipleChoiceOption(
                2,
                "Artikkelen har mange skrivefeil og rare bilder",
                "placeholder.jpg",
                true,
                "Riktig! Falske nyheter har ofte dårlig språk og manipulerte bilder."
            ),
            new MultipleChoiceOption(
                3,
                "Artikkelen handler om noe du ikke visste fra før",
                "placeholder.jpg",
                false,
                "At noe er nytt for deg betyr ikke at det er falskt. Sjekk kilden i stedet."
            )
        )
    );
  }

  private void seedQuestion(Level level, String questionText, List<MultipleChoiceOption> options) {
    optionValidator.validate(List.copyOf(options), level.getLevelType());

    Question question = new Question();
    question.setQuestionText(questionText);
    question.setLevel(level);
    question.setOptionContent(writeOptions(options));
    questionRepository.save(question);
  }

  private String writeOptions(List<MultipleChoiceOption> options) {
    try {
      return new ObjectMapper()
          .writeValueAsString(options);
    } catch (JacksonException e) {
      log.error("Error while writing options json", e);
      throw new IllegalStateException("Failed to serialize options", e);
    }
  }
}
