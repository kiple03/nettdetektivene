package org.ntnu.backend.security;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.ntnu.backend.user.model.UserModel;

@Entity
@Getter
@Table(name = "refresh_tokens")
public class RefreshTokenModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @Column(nullable = false)
  private Instant expiresAt;
}
