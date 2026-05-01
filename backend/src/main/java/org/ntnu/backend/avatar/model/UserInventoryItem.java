package org.ntnu.backend.avatar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.user.model.User;

/**
 * Represents a user's ownership of an avatar item, stored in the {@code user_avatar_unlock} table.
 * Tracks which item was unlocked, when it was acquired, and whether the user currently has it equipped.
 */
@Entity
@Table(name = "user_avatar_unlock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The user who owns this inventory entry. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The avatar item associated with this inventory entry. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avatar_item_id", nullable = false)
    private AvatarItem avatarItem;

    /** Timestamp of when the user unlocked this item. */
    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    /** Whether this item is currently equipped on the user's avatar. */
    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped;
}
