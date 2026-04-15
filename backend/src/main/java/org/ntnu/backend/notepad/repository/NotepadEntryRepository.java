package org.ntnu.backend.notepad.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.notepad.model.NotepadEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotepadEntryRepository extends JpaRepository<NotepadEntry, UUID> {

    @Query("""
        SELECT n FROM NotepadEntry n
        LEFT JOIN FETCH n.stop
        WHERE n.enrollment.id = :enrollmentId
        """)
    List<NotepadEntry> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
