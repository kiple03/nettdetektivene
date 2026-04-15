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

    @Query("""
        SELECT lc FROM LevelCompletion lc
        JOIN FETCH lc.level
        WHERE lc.enrollment.id = :enrollmentId
        """)
    List<LevelCompletion> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);

    @Query("""
        SELECT lc FROM LevelCompletion lc
        JOIN FETCH lc.enrollment e
        JOIN FETCH e.user
        JOIN FETCH e.classroom
        WHERE EXISTS (
            SELECT ct FROM ClassroomTeacher ct
            WHERE ct.classroom = e.classroom AND ct.user.id = :teacherId
        )
        ORDER BY lc.completedAt DESC
        LIMIT 20
        """)
    List<LevelCompletion> findRecentByTeacherId(@Param("teacherId") UUID teacherId);
}
