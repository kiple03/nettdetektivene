package org.ntnu.backend.stop.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.stop.model.Medal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedalRepository extends JpaRepository<Medal, UUID> {

    long countByEnrollmentId(UUID enrollmentId);

    List<Medal> findByEnrollmentId(UUID enrollmentId);
}
