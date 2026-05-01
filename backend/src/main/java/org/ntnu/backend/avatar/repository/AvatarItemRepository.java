package org.ntnu.backend.avatar.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link AvatarItem} entities, providing CRUD and custom
 * unlock-condition queries.
 */
public interface AvatarItemRepository extends JpaRepository<AvatarItem, UUID> {

    /**
     * Returns all avatar items that have a non-null unlock condition,
     * i.e. items that require a specific action or achievement to unlock.
     *
     * @return list of items with an associated unlock condition
     */
    List<AvatarItem> findByUnlockConditionNotNull();
}
