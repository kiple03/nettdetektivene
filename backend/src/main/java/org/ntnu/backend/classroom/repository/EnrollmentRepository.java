package org.ntnu.backend.classroom.repository;

import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

  boolean existsByUserAndLeftAtIsNull(User user);

  Optional<Enrollment> findByUserAndLeftAtIsNull(User user);
}
