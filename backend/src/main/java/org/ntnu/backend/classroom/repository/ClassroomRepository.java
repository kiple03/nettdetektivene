package org.ntnu.backend.classroom.repository;

import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for classroom; JPA's mySQL queries towards the database. 
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {

  /**
   * Checks whether a classroom with the given join code exists.
   *
   * @param joinCode the unique join code to check
   * @return {@code true} if a classroom with that join code exists
   */
  boolean existsByJoinCode(String joinCode);

  /**
   * Retrieves a classroom by its join code.
   *
   * @param joinCode the unique join code
   * @return an {@link Optional} containing the matching classroom, or empty if not found
   */
  Optional<Classroom> findByJoinCode(String joinCode);
}
