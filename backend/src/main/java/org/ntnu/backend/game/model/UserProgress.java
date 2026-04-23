package org.ntnu.backend.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.ntnu.backend.user.model.User;

@Entity
@Getter
@Setter
@Table(name="user_progress")
public class UserProgress {

  @Id
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "current_stop_id", nullable = false)
  private Stop currentStop;

  @Column(nullable = false)
  private int currentLevelOrder;

  @Column(nullable = false)
  private boolean gameCompleted;


  public static UserProgress createInitial(User student, Stop firstStop) {
    UserProgress progress = new UserProgress();
    progress.setUser(student);
    progress.setCurrentStop(firstStop);
    progress.setCurrentLevelOrder(1);
    progress.setGameCompleted(false);
    return progress;
  }
}
