package org.ntnu.backend.medalion.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.ntnu.backend.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a medalion awarded to a user upon completing an achievement.
 * Each medalion is tied to a specific user and records when it was earned.
 */
@Entity
@Table(name = "medal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medalion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false)
    private String description;

    @Column
    private LocalDateTime earned_at;
}   
