package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Classroom;
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

  @Query("SELECT ct.classroom FROM ClassroomTeacher ct WHERE ct.user.id = :teacherId")
  List<Classroom> findClassroomsByTeacherId(UUID teacherId);
}
