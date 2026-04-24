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

@Service
public class ProfanityFilterService {

    private final List<Pattern> patterns = new ArrayList<>();

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

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) return false;
        return patterns.stream().anyMatch(p -> p.matcher(text).find());
    }

    public String censor(String text) {
        if (text == null) return null;
        String result = text;
        for (Pattern p : patterns) {
            result = p.matcher(result).replaceAll("***");
        }
        return result;
    }
}
