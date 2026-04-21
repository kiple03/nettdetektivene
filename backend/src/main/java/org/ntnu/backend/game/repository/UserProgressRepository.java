package org.ntnu.backend.game.repository;

import java.util.UUID;
import org.ntnu.backend.game.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

}
