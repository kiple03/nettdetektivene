package org.ntnu.backend.game.model;

import java.time.Instant;
import java.util.UUID;

import org.ntnu.backend.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Tracks a student's position and completion state within the game. */
@Entity
@Getter
@Setter
@Table(name="user_progress")
public class UserProgress {

  /** Shared primary key with {@link User}. */
  @Id
  private UUID id;

  /** The student this progress record belongs to. */
  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  /** The stop the student is currently at. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "current_stop_id", nullable = false)
  private Stop currentStop;

  /** Order index of the level the student is currently on within the stop. */
  @Column(nullable = false)
  private int currentLevelOrder;

  /** Whether the student has completed the entire game. */
  @Column(nullable = false)
  private boolean gameCompleted;

  /** Timestamp of the most recent progress update. */
  @Column
  private Instant lastProgressAt;


  /**
   * Creates an initial progress record for a new student.
   *
   * @param student   the student
   * @param firstStop the first stop in the game
   * @return a new {@code UserProgress} starting at level 1 of the first stop
   */
  public static UserProgress createInitial(User student, Stop firstStop) {
    UserProgress progress = new UserProgress();
    progress.setUser(student);
    progress.setCurrentStop(firstStop);
    progress.setCurrentLevelOrder(1);
    progress.setGameCompleted(false);
    progress.setLastProgressAt(Instant.now());
    return progress;
  }
}
