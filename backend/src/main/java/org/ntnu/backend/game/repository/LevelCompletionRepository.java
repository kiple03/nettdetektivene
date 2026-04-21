package org.ntnu.backend.game.repository;

import java.util.List;
import java.util.UUID;

import org.ntnu.backend.game.model.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link LevelCompletion} entities, keyed by {@link UUID}.
 *
 * <p>Custom queries use {@code JOIN FETCH} to eagerly load associated levels, enrollments,
 * users, and classrooms in a single query, avoiding N+1 select problems.
 */
@Repository
public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, UUID> {

    /**
     * Returns the total number of levels completed for the given enrollment.
     *
     * @param enrollmentId the ID of the enrollment to count completions for
     * @return the number of completed levels
     */
    long countByEnrollmentId(UUID enrollmentId);

    /**
     * Deletes all level completions belonging to the given classroom.
     *
     * <p>Intended for use when a classroom is disbanded or reset, to cascade-clean
     * completion data not covered by a database-level cascade.
     *
     * @param classroomId the ID of the classroom whose completions should be removed
     */
    void deleteByEnrollmentClassroomId(UUID classroomId);

    /**
     * Returns all level completions for the given enrollment, with levels eagerly fetched.
     *
     * @param enrollmentId the ID of the enrollment to query completions for
     * @return a list of {@link LevelCompletion} instances, or an empty list if none exist
     */
    @Query("""
        SELECT lc FROM LevelCompletion lc
        JOIN FETCH lc.level
        WHERE lc.enrollment.id = :enrollmentId
        """)
    List<LevelCompletion> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);

    /**
     * Returns the 20 most recent level completions across all classrooms managed by the
     * given teacher, ordered by completion time descending.
     *
     * <p>Teacher ownership is verified via a subquery against {@code ClassroomTeacher};
     * only completions from classrooms where the teacher is explicitly assigned are included.
     * Enrollments, users, and classrooms are eagerly fetched to avoid N+1 queries.
     *
     * @param teacherId the ID of the teacher whose classrooms should be searched
     * @return a list of up to 20 {@link LevelCompletion} instances, newest first
     */
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
