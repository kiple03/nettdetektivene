package org.ntnu.backend.classroom.repository;

import java.util.UUID;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, ClassroomTeacherId> {

  boolean existsByClassroomIdAndUserId(UUID classroomId, UUID userId);
}
