package org.ntnu.backend.user.service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ProfanityFilterService {

    private final List<Pattern> patterns = new ArrayList<>();
    private final ConcurrentHashMap<UUID, List<Pattern>> classroomPatterns = new ConcurrentHashMap<>();

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

    public void putClassroomPatterns(UUID classroomId, List<String> words) {
        List<Pattern> compiled = new ArrayList<>(words.size());
        for (String word : words) {
            compiled.add(Pattern.compile("\\b" + Pattern.quote(word) + "\\b",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }
        classroomPatterns.put(classroomId, Collections.unmodifiableList(compiled));
    }

    public void evictClassroomCache(UUID classroomId) {
        classroomPatterns.remove(classroomId);
    }

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) return false;
        return patterns.stream().anyMatch(p -> p.matcher(text).find());
    }

    public boolean containsProfanity(String text, UUID classroomId) {
        if (text == null || text.isBlank()) return false;
        if (containsProfanity(text)) return true;
        if (classroomId == null) return false;
        List<Pattern> extra = classroomPatterns.get(classroomId);
        if (extra == null || extra.isEmpty()) return false;
        return extra.stream().anyMatch(p -> p.matcher(text).find());
    }

    public boolean containsProfanity(String text, List<String> extraWords) {
        if (text == null || text.isBlank()) return false;
        if (containsProfanity(text)) return true;
        if (extraWords == null || extraWords.isEmpty()) return false;
        return extraWords.stream().anyMatch(word ->
                Pattern.compile("\\b" + Pattern.quote(word) + "\\b",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                        .matcher(text).find());
    }

    public String censor(String text) {
        if (text == null) return null;
        String result = text;
        for (Pattern p : patterns) {
            result = p.matcher(result).replaceAll("***");
        }
        return result;
    }

    public String censor(String text, UUID classroomId) {
        if (text == null) return null;
        String result = censor(text);
        if (classroomId == null) return result;
        List<Pattern> extra = classroomPatterns.get(classroomId);
        if (extra == null) return result;
        for (Pattern p : extra) {
            result = p.matcher(result).replaceAll("***");
        }
        return result;
    }
}
