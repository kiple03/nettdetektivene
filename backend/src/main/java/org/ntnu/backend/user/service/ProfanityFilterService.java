package org.ntnu.backend.user.service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Detects and censors profanity using a word list loaded from {@code profanity-list.txt}
 * on the classpath, with optional per-call extra words.
 */
@Service
public class ProfanityFilterService {

    private final List<Pattern> patterns = new ArrayList<>();

    /** Loads the profanity word list and compiles each entry into a case-insensitive word-boundary pattern. */
    @PostConstruct
    void init() throws Exception {
        ClassPathResource resource = new ClassPathResource("profanity-list.txt");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    patterns.add(Pattern.compile("\\b" + Pattern.quote(word) + "\\b",
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                }
            }
        }
    }

    /**
     * Returns {@code true} if {@code text} contains any word from the built-in list.
     *
     * @param text the text to check; blank/null is safe and returns {@code false}
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) return false;
        return patterns.stream().anyMatch(p -> p.matcher(text).find());
    }

    /**
     * Returns {@code true} if {@code text} contains a word from the built-in list or {@code extraWords}.
     *
     * @param text       the text to check
     * @param extraWords additional words to match (e.g. classroom-specific censor list)
     */
    public boolean containsProfanity(String text, List<String> extraWords) {
        if (text == null || text.isBlank()) return false;
        if (containsProfanity(text)) return true;
        if (extraWords == null || extraWords.isEmpty()) return false;
        return extraWords.stream().anyMatch(word ->
                Pattern.compile("\\b" + Pattern.quote(word) + "\\b",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                        .matcher(text).find());
    }

    /**
     * Replaces all profane words in {@code text} with {@code ***}.
     *
     * @param text the text to censor; {@code null} returns {@code null}
     * @return censored copy of {@code text}
     */
    public String censor(String text) {
        if (text == null) return null;
        String result = text;
        for (Pattern p : patterns) {
            result = p.matcher(result).replaceAll("***");
        }
        return result;
    }

    /**
     * Replaces profane words (built-in list + {@code extraWords}) in {@code text} with {@code ***}.
     *
     * @param text       the text to censor
     * @param extraWords additional words to censor
     * @return censored copy of {@code text}
     */
    public String censor(String text, List<String> extraWords) {
        if (text == null) return null;
        String result = censor(text);
        if (extraWords == null || extraWords.isEmpty()) return result;
        for (String word : extraWords) {
            result = Pattern.compile("\\b" + Pattern.quote(word) + "\\b",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                    .matcher(result).replaceAll("***");
        }
        return result;
    }
}
