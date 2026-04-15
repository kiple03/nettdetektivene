package org.ntnu.backend.stop.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.stop.model.Medal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedalRepository extends JpaRepository<Medal, UUID> {

    long countByEnrollmentId(UUID enrollmentId);

    @Query("""
        SELECT m FROM Medal m
        JOIN FETCH m.stop
        WHERE m.enrollment.id = :enrollmentId
        """)
    List<Medal> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
