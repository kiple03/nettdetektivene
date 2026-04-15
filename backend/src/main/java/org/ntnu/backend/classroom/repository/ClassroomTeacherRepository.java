package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, ClassroomTeacherId> {

    List<ClassroomTeacher> findAllByIdUserId(UUID userId);

    boolean existsByIdClassroomIdAndIdUserId(UUID classroomId, UUID userId);
}
