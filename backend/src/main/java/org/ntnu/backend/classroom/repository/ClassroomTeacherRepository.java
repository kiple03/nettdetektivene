package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repsitory for classroomTeacher join table. JPA's mySQL queries towards this.
 */
@Repository
public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, ClassroomTeacherId> {

  boolean existsByClassroomIdAndUserId(UUID classroomId, UUID userId);

  void deleteByClassroomId(UUID classroomId);

  @Query("SELECT ct FROM ClassroomTeacher ct JOIN FETCH ct.user WHERE ct.id.classroomId = :classroomId")
  List<ClassroomTeacher> findWithUsersByClassroomId(@Param("classroomId") UUID classroomId);

  @Query("SELECT ct FROM ClassroomTeacher ct WHERE ct.id.classroomId = :classroomId AND ct.id.userId = :userId")
  Optional<ClassroomTeacher> findOneByClassroomIdAndUserId(@Param("classroomId") UUID classroomId, @Param("userId") UUID userId);

  @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN true ELSE false END FROM ClassroomTeacher ct WHERE ct.id.classroomId = :classroomId AND ct.id.userId = :userId AND ct.isOwner = true")
  boolean isOwnerOfClassroom(@Param("classroomId") UUID classroomId, @Param("userId") UUID userId);

    @Query("""
        SELECT ct.classroom.id, ct.classroom.title, ct.classroom.description,
               ct.classroom.joinCode, ct.classroom.createdAt,
               COUNT(DISTINCT e.id),
               (SELECT COUNT(lc.id) FROM LevelCompletion lc
                WHERE lc.enrollment.classroom = ct.classroom
                AND lc.enrollment.leftAt IS NULL)
        FROM ClassroomTeacher ct
        LEFT JOIN Enrollment e ON e.classroom = ct.classroom AND e.leftAt IS NULL
        WHERE ct.id.userId = :teacherId
        GROUP BY ct.classroom.id, ct.classroom.title, ct.classroom.description,
                 ct.classroom.joinCode, ct.classroom.createdAt
        """)
    List<Object[]> findClassroomsWithStudentCountByTeacherId(@Param("teacherId") UUID teacherId);
}
