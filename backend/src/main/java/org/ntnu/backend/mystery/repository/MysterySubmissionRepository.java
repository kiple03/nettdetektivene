package org.ntnu.backend.mystery.repository;

import java.util.UUID;
import org.ntnu.backend.mystery.model.MysterySubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysterySubmissionRepository extends JpaRepository<MysterySubmission, UUID> {
}
