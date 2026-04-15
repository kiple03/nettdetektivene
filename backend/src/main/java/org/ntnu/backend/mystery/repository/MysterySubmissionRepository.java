package org.ntnu.backend.mystery.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.mystery.model.MysterySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MysterySubmissionRepository extends JpaRepository<MysterySubmission, UUID> {

    List<MysterySubmission> findByEnrollmentClassroomId(UUID classroomId);

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
