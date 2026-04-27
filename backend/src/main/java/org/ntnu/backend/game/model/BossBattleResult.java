package org.ntnu.backend.game.model;

import java.time.Instant;
import java.util.UUID;

import org.ntnu.backend.classroom.model.Enrollment;

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

@Entity
@Table(name = "boss_battle_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BossBattleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(nullable = false)
    private boolean won;

    @Column(name = "time_seconds", nullable = false)
    private int timeSeconds;

    @Column(name = "player_hp_remaining", nullable = false)
    private int playerHpRemaining;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
