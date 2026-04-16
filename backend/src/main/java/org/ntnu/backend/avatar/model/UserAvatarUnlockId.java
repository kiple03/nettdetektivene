package org.ntnu.backend.avatar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserAvatarUnlockId implements Serializable {

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "avatar_item_id", length = 36)
    private UUID avatarItemId;
}
