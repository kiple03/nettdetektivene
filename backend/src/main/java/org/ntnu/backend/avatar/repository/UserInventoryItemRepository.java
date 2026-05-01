package org.ntnu.backend.avatar.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing {@link UserInventoryItem} entities.
 * Provides queries for retrieving items owned or equipped by users.
 */
public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, UUID> {

    /**
     * Returns all currently equipped items for a single user.
     *
     * @param userId the user's UUID
     * @return list of equipped inventory items
     */
    List<UserInventoryItem> findByUserIdAndIsEquippedTrue(UUID userId);

    /**
     * Returns all currently equipped items for a set of users — used for
     * bulk avatar rendering (e.g. leaderboard avatars).
     *
     * @param userIds list of user UUIDs to query
     * @return list of equipped inventory items across all specified users
     */
    List<UserInventoryItem> findByUserIdInAndIsEquippedTrue(List<UUID> userIds);

    /**
     * Returns every inventory item owned by a user, regardless of equipped state.
     *
     * @param userid the user's UUID
     * @return all inventory items for the user
     */
    List<UserInventoryItem> findByUserId(UUID userid);

    /**
     * Returns all inventory items for a user, sorted by item name ascending.
     *
     * @param userId the user's UUID
     * @return inventory items sorted alphabetically by avatar item name
     */
    List<UserInventoryItem> findByUserIdOrderByAvatarItemNameAsc(UUID userId);
}
