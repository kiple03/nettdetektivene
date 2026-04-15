package org.ntnu.backend.enrollment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    long countByClassroomIdAndLeftAtIsNull(UUID classroomId);

    List<Enrollment> findByClassroomIdAndLeftAtIsNull(UUID classroomId);

    Optional<Enrollment> findByClassroomIdAndUserIdAndLeftAtIsNull(UUID classroomId, UUID userId);

    @Query("""
        SELECT e.user.id, e.user.username,
               COUNT(DISTINCT m.id),
               COUNT(DISTINCT lc.id)
        FROM Enrollment e
        LEFT JOIN Medal m ON m.enrollment = e
        LEFT JOIN LevelCompletion lc ON lc.enrollment = e
        WHERE e.classroom.id = :classroomId AND e.leftAt IS NULL
        GROUP BY e.user.id, e.user.username
        """)
    List<Object[]> findStudentCountsByClassroomId(@Param("classroomId") UUID classroomId);
}
