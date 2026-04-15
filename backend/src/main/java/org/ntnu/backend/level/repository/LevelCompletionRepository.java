package org.ntnu.backend.level.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.level.model.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, UUID> {

    long countByEnrollmentId(UUID enrollmentId);

    List<LevelCompletion> findByEnrollmentId(UUID enrollmentId);

    @Query("""
        SELECT lc FROM LevelCompletion lc, ClassroomTeacher ct
        WHERE lc.enrollment.classroom = ct.classroom
        AND ct.user.id = :teacherId
        ORDER BY lc.completedAt DESC
        LIMIT 20
        """)
    List<LevelCompletion> findRecentByTeacherId(@Param("teacherId") UUID teacherId);
}
