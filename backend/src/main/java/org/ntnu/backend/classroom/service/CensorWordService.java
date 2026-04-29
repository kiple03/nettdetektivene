package org.ntnu.backend.classroom.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.AddCensorWordRequest;
import org.ntnu.backend.classroom.dto.CensorWordResponse;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CensorWordService {

    private final ClassroomCensorWordRepository censorWordRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final ProfanityFilterService profanityFilterService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void warmCacheOnStartup() {
        Map<UUID, List<ClassroomCensorWord>> byClassroom = censorWordRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(w -> w.getClassroom().getId()));
        byClassroom.forEach((classroomId, words) -> syncCache(classroomId, words));
    }

    @Transactional(readOnly = true)
    public List<CensorWordResponse> listWords(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        List<ClassroomCensorWord> words = censorWordRepository.findByClassroomId(classroomId);
        syncCache(classroomId, words);
        return words.stream().map(this::toResponse).toList();
    }

    public CensorWordResponse addWord(UUID classroomId, AddCensorWordRequest request,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        String normalized = request.word().trim().toLowerCase();
        if (censorWordRepository.existsByClassroomIdAndWord(classroomId, normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ordet finnes allerede i listen");
        }

        var classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet"));

        ClassroomCensorWord entity = ClassroomCensorWord.builder()
                .classroom(classroom)
                .word(normalized)
                .build();
        ClassroomCensorWord saved = censorWordRepository.save(entity);

        List<ClassroomCensorWord> allWords = censorWordRepository.findByClassroomId(classroomId);
        syncCache(classroomId, allWords);

        return toResponse(saved);
    }

    public void deleteWord(UUID classroomId, UUID wordId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        ClassroomCensorWord word = censorWordRepository.findByIdAndClassroomId(wordId, classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordet ble ikke funnet"));

        censorWordRepository.delete(word);

        List<ClassroomCensorWord> remaining = censorWordRepository.findByClassroomId(classroomId);
        syncCache(classroomId, remaining);
    }

    private void syncCache(UUID classroomId, List<ClassroomCensorWord> words) {
        List<String> wordStrings = words.stream().map(ClassroomCensorWord::getWord).toList();
        profanityFilterService.putClassroomPatterns(classroomId, wordStrings);
    }

    private CensorWordResponse toResponse(ClassroomCensorWord entity) {
        return CensorWordResponse.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
