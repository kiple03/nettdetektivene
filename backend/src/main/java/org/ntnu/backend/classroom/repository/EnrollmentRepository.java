package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

  boolean existsByUserAndLeftAtIsNull(User user);

  Optional<Enrollment> findByUserAndLeftAtIsNull(User user);

  long countByClassroomIdAndLeftAtIsNull(UUID classroomId);

  List<Enrollment> findByClassroomIdAndLeftAtIsNull(UUID classroomId);

}
