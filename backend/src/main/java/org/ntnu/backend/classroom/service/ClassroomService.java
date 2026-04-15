package org.ntnu.backend.classroom.service;

import org.ntnu.backend.classroom.dto.*;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

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

    public ClassroomResponse createClassroom(CreateClassroomRequest request) {
        Classroom classroom = new Classroom();
        classroom.setTitle(request.getTitle());
        classroom.setDescription(request.getDescription());
        classroom.setJoinCode(generateUniqueCode());
        Classroom saved = classroomRepository.save(classroom);
        return toResponse(saved);
    }

    public ClassroomResponse getClassroom(int id) {
        return toResponse(findClassroom(id));
    }

    public ClassroomResponse updateClassroom(int id, UpdateClassroomRequest request) {
        Classroom classroom = findClassroom(id);
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            classroom.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            classroom.setDescription(request.getDescription().trim().isEmpty() ? null : request.getDescription().trim());
        }
        return toResponse(classroomRepository.save(classroom));
    }

    public void deleteClassroom(int id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet");
        }
        classroomRepository.deleteById(id);
    }

    private Classroom findClassroom(int id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Klasserom ikke funnet"));
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
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke generere unik kode");
    }

    private ClassroomResponse toResponse(Classroom c) {
        return new ClassroomResponse(c.getId(), c.getTitle(), c.getDescription(), c.getJoinCode());
    }
}