package org.ntnu.backend.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.ntnu.backend.user.model.enums.RoleEnum;

/**
 * JPA entity representing an application user, mapped to the {@code app_user} table.
 *
 * <p>A user can hold one of the roles defined in {@link RoleEnum} (e.g. {@code STUDENT} or
 * {@code TEACHER}), which controls portal access and feature visibility throughout the
 * application.
 *
 * <p>Passwords are never stored in plain text; the {@link #passwordHash} field holds a
 * BCrypt-encoded value. The field is nullable to support future OAuth / SSO users who
 * authenticate without a local password.
 *
 * <p>Lombok annotations generate boilerplate: {@code @Getter}/{@code @Setter} for accessors,
 * {@code @Builder} for a fluent construction API, and {@code @NoArgsConstructor}/
 * {@code @AllArgsConstructor} to satisfy JPA and builder requirements respectively.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class User {

  /**
   * Primary key, generated as a random UUID by the database.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The role assigned to this user, stored as its enum name (e.g. {@code "STUDENT"}).
   * Determines which portal the user may access.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoleEnum role;

  @Column(nullable = true)
  private String username;

  /**
   * The user's e-mail address. Used as the primary login identifier and must be unique.
   */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * BCrypt-encoded password. {@code null} for accounts that authenticate via an external
   * identity provider rather than a local password.
   */
  @Column(nullable = true)
  private String passwordHash;

  /**
   * Timestamp at which the account was created, set automatically by Hibernate on insert
   * and never updated thereafter.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
