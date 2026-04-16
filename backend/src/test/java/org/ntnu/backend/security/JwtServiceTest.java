package org.ntnu.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashSet;
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
  private UserPrincipal teacherPrincipal;
  private UserPrincipal studentPrincipal;
  private UUID classroomId;
  private final String secret = "dGVzdHNlY3JldGtleXRoYXRpc2xvbmdlbm91Z2hmb3JoczI1Ng==";

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", secret);
    ReflectionTestUtils.setField(jwtService, "expiration", 300000L);

    classroomId = UUID.randomUUID();

    User teacher = User.builder()
        .id(UUID.randomUUID())
        .username("testuser")
        .email("test@test.no")
        .passwordHash("encoded")
        .role(RoleEnum.TEACHER)
        .build();
    teacherPrincipal = new UserPrincipal(teacher);

    User student = User.builder()
        .id(UUID.randomUUID())
        .username("student1")
        .email("student@test.no")
        .passwordHash("encoded")
        .role(RoleEnum.STUDENT)
        .build();
    studentPrincipal = new UserPrincipal(student);
  }

  // ===== TOKEN GENERATION =====

  @Test
  void generateToken_containsExpectedClaims() {
    String token = jwtService.generateToken(teacherPrincipal, null);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.getSubject()).isEqualTo("testuser");
    assertThat(claims.get("userId", String.class))
        .isEqualTo(teacherPrincipal.getUser().getId().toString());
    assertThat(claims.get("role", String.class)).isEqualTo("TEACHER");
  }

  @Test
  void generateToken_setsExpiration() {
    String token = jwtService.generateToken(teacherPrincipal, null);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.getExpiration()).isAfter(new Date());
  }

  @Test
  void generateToken_studentRole() {
    String token = jwtService.generateToken(studentPrincipal, classroomId);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
  }

  // ===== PARSING & VALIDATION =====

  @Test
  void generateToken_withClassroomId_containsClaim() {
    String token = jwtService.generateToken(studentPrincipal, classroomId);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.get("classroomId", String.class))
        .isEqualTo(classroomId.toString());
  }

  @Test
  void parseAndValidate_expiredToken_throws() {
    ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
    String expired = jwtService.generateToken(teacherPrincipal, null);

    assertThatThrownBy(() -> jwtService.parseAndValidate(expired))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void parseAndValidate_tamperedToken_throws() {
    String token = jwtService.generateToken(teacherPrincipal, null);
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    assertThatThrownBy(() -> jwtService.parseAndValidate(tampered))
        .isInstanceOf(Exception.class);
  }

  @Test
  void parseAndValidate_wrongSigningKey_throws() {
    String token = jwtService.generateToken(teacherPrincipal, null);

    JwtService otherService = new JwtService();
    ReflectionTestUtils.setField(otherService, "secret",
        "b3RoZXJzZWNyZXRrZXl0aGF0aXNsb25nZW5vdWdoZm9yaHMyNTY=");
    ReflectionTestUtils.setField(otherService, "expiration", 300000L);

    assertThatThrownBy(() -> otherService.parseAndValidate(token))
        .isInstanceOf(Exception.class);
  }

  @Test
  void parseAndValidate_garbageString_throws() {
    assertThatThrownBy(() -> jwtService.parseAndValidate("not.a.jwt"))
        .isInstanceOf(Exception.class);
  }

  @Test
  void generateToken_withNullClassroomId_noClaim() {
    String token = jwtService.generateToken(studentPrincipal, null);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.get("classroomId", String.class)).isNull();
  }

  @Test
  void generateToken_teacher_noClassroomId() {
    String token = jwtService.generateToken(teacherPrincipal, null);
    Claims claims = jwtService.parseAndValidate(token);

    assertThat(claims.get("classroomId", String.class)).isNull();
  }
}