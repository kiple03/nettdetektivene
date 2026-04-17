package org.ntnu.backend.avatar.repository;

import java.util.UUID;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarItemRepository extends JpaRepository<AvatarItem, UUID> {
    
}
