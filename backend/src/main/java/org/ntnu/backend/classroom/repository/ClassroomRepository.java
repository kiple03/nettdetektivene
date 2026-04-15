package org.ntnu.backend.classroom.repository;

import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    boolean existsByJoinCode(String joinCode);

    Optional<Classroom> findByJoinCode(String joinCode);
}