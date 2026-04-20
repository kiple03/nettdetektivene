package org.ntnu.backend.avatar.repository;

import java.util.UUID;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Java Repository for an individual avatar item. Designed for JPA to ask 
 * mySQL queries to database.
 */
public interface AvatarItemRepository extends JpaRepository<AvatarItem, UUID> {
    
}
