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
 * Repository for active classroom enrollments. "Active" means {@code leftAt IS NULL}.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

  /**
   * Returns whether the user currently has an active enrollment in any classroom.
   *
   * @param user the user to check
   * @return {@code true} if an active enrollment exists
   */
  boolean existsByUserAndLeftAtIsNull(User user);

  /**
   * Finds the user's current active enrollment.
   *
   * @param user the user whose enrollment to look up
   * @return the active enrollment, or empty if the user is not enrolled anywhere
   */
  Optional<Enrollment> findByUserAndLeftAtIsNull(User user);

  /**
   * Finds the active enrollment for a user by their ID.
   *
   * @param userId the user's ID
   * @return the active enrollment, or empty if not found
   */
  Optional<Enrollment> findByUserIdAndLeftAtIsNull(UUID userId);

  /**
   * Counts active students in a classroom.
   *
   * @param classroomId the classroom's ID
   * @return number of currently enrolled students
   */
  long countByClassroomIdAndLeftAtIsNull(UUID classroomId);

  /**
   * Lists all active enrollments in a classroom.
   *
   * @param classroomId the classroom's ID
   * @return list of active enrollments
   */
  List<Enrollment> findByClassroomIdAndLeftAtIsNull(UUID classroomId);

  /**
   * Finds a specific student's active enrollment in a classroom.
   *
   * @param classroomId the classroom's ID
   * @param userId      the student's ID
   * @return the enrollment, or empty if the student is not actively enrolled
   */
  Optional<Enrollment> findByClassroomIdAndUserIdAndLeftAtIsNull(UUID classroomId, UUID userId);

  /**
   * Loads all active enrollments with their associated user and classroom eagerly fetched.
   * Used when batch processing requires both relations without lazy-load overhead.
   *
   * @return list of active enrollments with user and classroom populated
   */
  @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.classroom WHERE e.leftAt IS NULL")
  List<Enrollment> findAllActiveWithUserAndClassroom();

  /**
   * Deletes all enrollments belonging to a classroom, used when a classroom is removed.
   *
   * @param classroomId the classroom whose enrollments to delete
   */
  void deleteByClassroomId(UUID classroomId);

  /**
   * Returns every active enrollment across all classrooms.
   *
   * @return all enrollments where {@code leftAt} is null
   */
  List<Enrollment> findAllByLeftAtIsNull();

  /**
   * Lists active enrollments in a classroom (alias used in some service layers).
   *
   * @param classroomId the classroom's ID
   * @return active enrollments for the given classroom
   */
  List<Enrollment> findAllByClassroomIdAndLeftAtIsNull(UUID classroomId);

  /**
   * Lists active enrollments across multiple classrooms in one query.
   *
   * @param classroomIds list of classroom IDs to query
   * @return active enrollments belonging to any of the given classrooms
   */
  List<Enrollment> findAllByClassroomIdInAndLeftAtIsNull(List<UUID> classroomIds);
}
