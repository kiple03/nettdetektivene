package org.ntnu.backend.stop.repository;

import java.util.UUID;
import org.ntnu.backend.stop.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {}
