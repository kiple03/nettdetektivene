package org.ntnu.backend.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<UserModel, UUID> {

  Optional<UserModel> findByEmail(String email);

  Optional<UserModel> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
