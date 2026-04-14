package org.ntnu.backend.classroom.repository;

import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    boolean existsByJoinCode(String joinCode);
}