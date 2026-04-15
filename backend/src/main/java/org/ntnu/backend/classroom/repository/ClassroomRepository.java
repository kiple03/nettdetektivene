package org.ntnu.backend.classroom.repository;

import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {

  boolean existsByJoinCode(String joinCode);

  Optional<Classroom> findByJoinCode(String joinCode);

  boolean existsByIdAndUsersId(UUID classroomId, UUID userId);
}
