package org.ntnu.backend.classroom.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for the {@link ClassroomTeacher} join table, which tracks which teachers belong to
 * which classrooms and whether they are owners.
 */
@Repository
public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, ClassroomTeacherId> {

  /**
   * @return {@code true} if a teacher–classroom link exists for the given IDs
   */
  boolean existsByClassroomIdAndUserId(UUID classroomId, UUID userId);

  /** Removes all teacher entries for the given classroom (used when deleting a classroom). */
  void deleteByClassroomId(UUID classroomId);

  /**
   * Fetches all teachers in a classroom with their user data eagerly loaded.
   *
   * @param classroomId target classroom
   * @return list of {@link ClassroomTeacher} entries with users populated
   */
  @Query("SELECT ct FROM ClassroomTeacher ct JOIN FETCH ct.user WHERE ct.id.classroomId = :classroomId")
  List<ClassroomTeacher> findWithUsersByClassroomId(@Param("classroomId") UUID classroomId);

  /**
   * Looks up a single teacher–classroom link by both IDs.
   *
   * @return the entry if it exists
   */
  @Query("SELECT ct FROM ClassroomTeacher ct WHERE ct.id.classroomId = :classroomId AND ct.id.userId = :userId")
  Optional<ClassroomTeacher> findOneByClassroomIdAndUserId(@Param("classroomId") UUID classroomId, @Param("userId") UUID userId);

  /**
   * @return {@code true} if the user is the owner of the classroom
   */
  @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN true ELSE false END FROM ClassroomTeacher ct WHERE ct.id.classroomId = :classroomId AND ct.id.userId = :userId AND ct.isOwner = true")
  boolean isOwnerOfClassroom(@Param("classroomId") UUID classroomId, @Param("userId") UUID userId);

  /**
   * Returns all classrooms that a given teacher belongs to (as either owner or co-teacher).
   *
   * @param teacherId the teacher's user ID
   */
  @Query("SELECT ct.classroom FROM ClassroomTeacher ct WHERE ct.user.id = :teacherId")
  List<Classroom> findClassroomsByTeacherId(UUID teacherId);
}
