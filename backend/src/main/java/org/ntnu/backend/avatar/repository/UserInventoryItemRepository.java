package org.ntnu.backend.avatar.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Java Repository for the users avatar items. Designed for JPA to ask 
 * mySQL queries to database.
 */
public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, UUID> {
    List<UserInventoryItem> findByUserIdAndIsEquippedTrue(UUID userId);
    List<UserInventoryItem> findByUserId(UUID userid);
}
