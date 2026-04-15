package org.ntnu.backend.security;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {

  private JwtService jwtService;
  private UserPrincipal principal;
  private final String secret = "dGVzdHNlY3JldGtleXRoYXRpc2xvbmdlbm91Z2hmb3JoczI1Ng=="; // base64 test key

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", secret);
    ReflectionTestUtils.setField(jwtService, "expiration", 300000L); // 5 min

    User user = User.builder()
        .id(UUID.randomUUID())
        .username("testuser")
        .email("test@test.no")
        .passwordHash("encoded")
        .role(RoleEnum.TEACHER)
        .build();

    principal = new UserPrincipal(user);
  }

  // ===== TOKEN GENERATION =====

  @Test
  void generateToken_containsExpectedClaims() {
    String token = jwtService.generateToken(principal);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.getSubject()).isEqualTo("testuser");
    assertThat(claims.get("userId", String.class)).isEqualTo(principal.getUser().getId().toString());
    assertThat(claims.get("role", String.class)).isEqualTo("TEACHER");
  }

  @Test
  void generateToken_setsExpiration() {
    String token = jwtService.generateToken(principal);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.getExpiration()).isAfter(new Date());
  }

  @Test
  void generateToken_studentRole() {
    User student = User.builder()
        .id(UUID.randomUUID())
        .username("student1")
        .email("student@test.no")
        .passwordHash("encoded")
        .role(RoleEnum.STUDENT)
        .build();

    String token = jwtService.generateToken(new UserPrincipal(student));
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
  }

  // ===== PARSING & VALIDATION =====

  @Test
  void parseAndValidate_validToken_returnsClaims() {
    String token = jwtService.generateToken(principal);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims).isNotNull();
    assertThat(claims.getSubject()).isEqualTo("testuser");
  }

  @Test
  void parseAndValidate_expiredToken_throws() {
    ReflectionTestUtils.setField(jwtService, "expiration", -1000L); // already expired
    String expired = jwtService.generateToken(principal);

    assertThatThrownBy(() -> jwtService.parseAndValidate(expired))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void parseAndValidate_tamperedToken_throws() {
    String token = jwtService.generateToken(principal);
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    assertThatThrownBy(() -> jwtService.parseAndValidate(tampered))
        .isInstanceOf(Exception.class); // SignatureException
  }

  @Test
  void parseAndValidate_garbageString_throws() {
    assertThatThrownBy(() -> jwtService.parseAndValidate("not.a.jwt"))
        .isInstanceOf(Exception.class);
  }

  @Test
  void parseAndValidate_wrongSigningKey_throws() {
    String token = jwtService.generateToken(principal);

    // Create a second service with a different secret
    JwtService otherService = new JwtService();
    ReflectionTestUtils.setField(otherService, "secret",
        "b3RoZXJzZWNyZXRrZXl0aGF0aXNsb25nZW5vdWdoZm9yaHMyNTY=");
    ReflectionTestUtils.setField(otherService, "expiration", 300000L);

    assertThatThrownBy(() -> otherService.parseAndValidate(token))
        .isInstanceOf(Exception.class);
  }
}
