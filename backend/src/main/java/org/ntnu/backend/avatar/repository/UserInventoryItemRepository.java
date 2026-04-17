package org.ntnu.backend.avatar.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, UUID> {
    List<UserInventoryItem> findByUserIdAndIsEquippedTrue(UUID userId);
}
