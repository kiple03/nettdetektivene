package org.ntnu.backend.avatar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A wearable cosmetic item that can be equipped on a student's avatar.
 * Items are grouped by category (e.g. hair, shirt) and may require a specific
 * unlock condition before a student can equip them.
 */
@Entity
@Table(name = "avatar_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    /** Grouping slot this item occupies on the avatar (e.g. "hair", "shirt", "accessory"). */
    @Column(length = 50, nullable = false)
    private String category;

    /** Human-readable description of what a student must achieve to unlock this item. Null means freely available. */
    @Column(name = "unlock_condition", length = 255)
    private String unlockCondition;

    /** Filename of the SVG/PNG asset used to render this item on the avatar. */
    @Column(name = "image_file", length = 100)
    private String imageFile;
}
