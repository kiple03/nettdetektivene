package org.ntnu.backend.enrollment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    long countByClassroomIdAndLeftAtIsNull(UUID classroomId);

    List<Enrollment> findByClassroomIdAndLeftAtIsNull(UUID classroomId);

    Optional<Enrollment> findByClassroomIdAndUserIdAndLeftAtIsNull(UUID classroomId, UUID userId);
}
