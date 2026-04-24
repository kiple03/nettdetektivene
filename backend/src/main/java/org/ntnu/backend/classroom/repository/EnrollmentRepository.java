package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repsiroty for enrollment join table. JPA's mySQL queries towards this.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

  boolean existsByUserAndLeftAtIsNull(User user);

  Optional<Enrollment> findByUserAndLeftAtIsNull(User user);

  long countByClassroomIdAndLeftAtIsNull(UUID classroomId);

  List<Enrollment> findByClassroomIdAndLeftAtIsNull(UUID classroomId);

  Optional<Enrollment> findByClassroomIdAndUserIdAndLeftAtIsNull(UUID classroomId, UUID userId);

  @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.classroom WHERE e.leftAt IS NULL")
  List<Enrollment> findAllActiveWithUserAndClassroom();

  void deleteByClassroomId(UUID classroomId);

  List<Enrollment> findAllByLeftAtIsNull();

  List<Enrollment> findAllByClassroomIdAndLeftAtIsNull(UUID classroomId);

  List<Enrollment> findAllByClassroomIdInAndLeftAtIsNull(List<UUID> classroomIds);
}
