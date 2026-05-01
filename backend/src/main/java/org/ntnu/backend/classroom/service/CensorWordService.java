package org.ntnu.backend.classroom.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.censor.AddCensorWordRequest;
import org.ntnu.backend.classroom.dto.censor.CensorWordResponse;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Manages censored words for classrooms. */
@Service
@RequiredArgsConstructor
@Transactional
public class CensorWordService {

    private final ClassroomCensorWordRepository censorWordRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomAuthorizationService classroomAuthorizationService;

    /**
     * Returns all censored words for a classroom.
     *
     * @param classroomId the classroom UUID
     * @param principal   the authenticated caller
     * @return list of censor word responses
     * @throws org.springframework.web.server.ResponseStatusException 403 if caller lacks access
     */
    @Transactional(readOnly = true)
    public List<CensorWordResponse> listWords(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return censorWordRepository.findByClassroomId(classroomId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Adds a censored word to a classroom (normalized to lowercase).
     *
     * @param classroomId the classroom UUID
     * @param request     word to add
     * @param principal   the authenticated caller
     * @return the created censor word
     * @throws org.springframework.web.server.ResponseStatusException 409 if word already exists, 404 if classroom not found
     */
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

        return toResponse(censorWordRepository.save(entity));
    }

    /**
     * Deletes a censored word from a classroom.
     *
     * @param classroomId the classroom UUID
     * @param wordId      the word UUID to remove
     * @param principal   the authenticated caller
     * @throws org.springframework.web.server.ResponseStatusException 404 if word not found in classroom
     */
    public void deleteWord(UUID classroomId, UUID wordId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        ClassroomCensorWord word = censorWordRepository.findByIdAndClassroomId(wordId, classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordet ble ikke funnet"));

        censorWordRepository.delete(word);
    }

    private CensorWordResponse toResponse(ClassroomCensorWord entity) {
        return CensorWordResponse.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
