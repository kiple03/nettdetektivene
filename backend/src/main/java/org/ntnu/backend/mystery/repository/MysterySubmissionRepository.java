package org.ntnu.backend.mystery.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.mystery.model.MysterySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link MysterySubmission} entities, keyed by {@link UUID}.
 *
 * <p>Custom queries use {@code JOIN FETCH} to eagerly load associated enrollments,
 * users, and classrooms in a single query, avoiding N+1 select problems.
 */
public interface MysterySubmissionRepository extends JpaRepository<MysterySubmission, UUID> {

    /**
     * Deletes all mystery submissions belonging to the given classroom.
     *
     * <p>Intended for use when a classroom is disbanded or reset, to cascade-clean
     * submission data that is not covered by a database-level cascade.
     *
     * @param classroomId the ID of the classroom whose submissions should be removed
     */
    void deleteByEnrollmentClassroomId(UUID classroomId);

    /**
     * Returns all mystery submissions for the given classroom, with enrollments and
     * users eagerly fetched.
     *
     * @param classroomId the ID of the classroom to query submissions for
     * @return a list of {@link MysterySubmission} instances, or an empty list if none exist
     */
    @Query("""
        SELECT ms FROM MysterySubmission ms
        JOIN FETCH ms.enrollment e
        JOIN FETCH e.user
        WHERE e.classroom.id = :classroomId
        """)
    List<MysterySubmission> findByEnrollmentClassroomId(@Param("classroomId") UUID classroomId);

    /**
     * Returns the 20 most recent mystery submissions across all classrooms managed by the
     * given teacher, ordered by submission time descending.
     *
     * <p>Teacher ownership is verified via a subquery against {@code ClassroomTeacher};
     * only submissions from classrooms where the teacher is explicitly assigned are included.
     * Enrollments, users, and classrooms are eagerly fetched to avoid N+1 queries.
     *
     * @param teacherId the ID of the teacher whose classrooms should be searched
     * @return a list of up to 20 {@link MysterySubmission} instances, newest first
     */
    @Query("""
        SELECT ms FROM MysterySubmission ms
        JOIN FETCH ms.enrollment e
        JOIN FETCH e.user
        JOIN FETCH e.classroom
        WHERE EXISTS (
            SELECT ct FROM ClassroomTeacher ct
            WHERE ct.classroom = e.classroom AND ct.user.id = :teacherId
        )
        ORDER BY ms.submittedAt DESC
        LIMIT 20
        """)
    List<MysterySubmission> findRecentByTeacherId(@Param("teacherId") UUID teacherId);
}
