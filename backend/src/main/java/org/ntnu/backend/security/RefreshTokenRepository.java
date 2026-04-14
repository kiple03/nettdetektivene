package org.ntnu.backend.security;

import java.util.Optional;
import org.ntnu.backend.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {

  Optional<RefreshTokenModel> findByToken(String token);

  void deleteByToken(String token);

  void deleteAllByUser(UserModel user);
}
