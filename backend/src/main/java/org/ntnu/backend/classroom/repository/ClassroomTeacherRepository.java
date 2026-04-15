package org.ntnu.backend.classroom.repository;

import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomTeacherRepository extends JpaRepository<ClassroomTeacher, ClassroomTeacherId> {
}
