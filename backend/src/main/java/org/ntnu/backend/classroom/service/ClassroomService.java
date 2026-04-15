package org.ntnu.backend.classroom.service;

import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class ClassroomService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 20;

    private final ClassroomRepository classroomRepository;
    private final SecureRandom random = new SecureRandom();

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    // ── Teacher operations ────────────────────────────────────────────────────

    public ClassroomResponse createClassroom(CreateClassroomRequest request) {
        Classroom classroom = new Classroom();
        classroom.setTitle(request.getTitle());
        classroom.setDescription(request.getDescription());
        classroom.setJoinCode(generateUniqueCode());
        Classroom saved = classroomRepository.save(classroom);
        return toResponse(saved);
    }

    public ClassroomResponse updateClassroom(String id, UpdateClassroomRequest request) {
        Classroom classroom = findClassroom(UUID.fromString(id));
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            classroom.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            classroom.setDescription(
                    request.getDescription().trim().isEmpty()
                            ? null
                            : request.getDescription().trim()
            );
        }
        return toResponse(classroomRepository.save(classroom));
    }

    public void deleteClassroom(String id) {
        UUID uuid = UUID.fromString(id);
        if (!classroomRepository.existsById(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet");
        }
        classroomRepository.deleteById(uuid);
    }

    // ── Shared operations ─────────────────────────────────────────────────────

    public ClassroomResponse getClassroom(String id) {
        return toResponse(findClassroom(UUID.fromString(id)));
    }

    // ── Student operations ────────────────────────────────────────────────────

    public ClassroomResponse joinClassroom(JoinClassroomRequest request) {
        Classroom classroom = classroomRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ugyldig kode – klasserom ikke funnet"));
        return toResponse(classroom);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Classroom findClassroom(UUID id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Klasserom ikke funnet"));
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int j = 0; j < CODE_LENGTH; j++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!classroomRepository.existsByJoinCode(code)) {
                return code;
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke generere unik kode");
    }

    private ClassroomResponse toResponse(Classroom c) {
        return new ClassroomResponse(c.getId().toString(), c.getTitle(), c.getDescription(), c.getJoinCode());
    }
}
