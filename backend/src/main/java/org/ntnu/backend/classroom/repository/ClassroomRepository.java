package org.ntnu.backend.classroom.repository;

import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    boolean existsByJoinCode(String joinCode);

    Optional<Classroom> findByJoinCode(String joinCode);
}
