package org.ntnu.backend.avatar.repository;

import org.ntnu.backend.avatar.model.UserAvatarUnlock;
import org.ntnu.backend.avatar.model.UserAvatarUnlockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarUnlockRepository extends JpaRepository<UserAvatarUnlock, UserAvatarUnlockId> {}
