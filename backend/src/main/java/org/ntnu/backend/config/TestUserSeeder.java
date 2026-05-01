package org.ntnu.backend.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.model.BossBattleResult;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.BossBattleResultRepository;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.medalion.model.Medalion;
import org.ntnu.backend.medalion.repository.MedalionRepository;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;
import org.ntnu.backend.notes.repository.StudentLevelNoteRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent seeder that creates a test teacher, a test classroom, and a fully-unlocked
 * test student. Runs after {@link GameSeeder} and {@link AvatarDataSeeder} so that all
 * stops, levels, note templates, and avatar items are present before linking the student
 * to them.
 *
 * <p>"Fully unlocked" means: all avatar items in inventory, all medals awarded, game
 * progress marked complete, a winning boss battle result, and one granted level note
 * per existing template.
 *
 * <p>Credentials and classroom code are documented in the public README.
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class TestUserSeeder implements CommandLineRunner {

  private static final String TEACHER_EMAIL = "teacher@test.local";
  private static final String STUDENT_EMAIL = "student@test.local";
  private static final String TEST_PASSWORD = "password123";
  private static final String CLASSROOM_TITLE = "Test Classroom";
  private static final String CLASSROOM_JOIN_CODE = "TEST01";

  private final UserRepository userRepository;
  private final ClassroomRepository classroomRepository;
  private final ClassroomTeacherRepository classroomTeacherRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final LevelNoteTemplateRepository levelNoteTemplateRepository;
  private final StudentLevelNoteRepository studentLevelNoteRepository;
  private final AvatarItemRepository avatarItemRepository;
  private final UserInventoryItemRepository userInventoryItemRepository;
  private final MedalionRepository medalionRepository;
  private final UserProgressRepository userProgressRepository;
  private final BossBattleResultRepository bossBattleResultRepository;

  @Override
  @Transactional
  public void run(String... args) {
    log.info("TestUserSeeder: ensuring test teacher, classroom, and fully-unlocked student exist...");

    User teacher = upsertUser(TEACHER_EMAIL, "test_teacher", RoleEnum.TEACHER);
    User student = upsertUser(STUDENT_EMAIL, "test_student", RoleEnum.STUDENT);

    Classroom classroom = upsertClassroom();
    upsertClassroomTeacher(classroom, teacher);
    Enrollment enrollment = upsertEnrollment(classroom, student);

    unlockAllAvatarItems(student);
    completeAllProgress(student);
    awardAllMedals(student);
    grantAllLevelNotes(student);
    upsertWinningBossResult(enrollment);

    log.info("TestUserSeeder: done");
  }

  private User upsertUser(String email, String username, RoleEnum role) {
    return userRepository.findByEmail(email).orElseGet(() -> {
      log.info("TestUserSeeder: creating user {}", email);
      return userRepository.save(User.builder()
          .email(email)
          .username(username)
          .role(role)
          .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
          .build());
    });
  }

  private Classroom upsertClassroom() {
    return classroomRepository.findByJoinCode(CLASSROOM_JOIN_CODE).orElseGet(() -> {
      log.info("TestUserSeeder: creating classroom {}", CLASSROOM_JOIN_CODE);
      return classroomRepository.save(Classroom.builder()
          .title(CLASSROOM_TITLE)
          .description("Seeded classroom owned by the test teacher.")
          .joinCode(CLASSROOM_JOIN_CODE)
          .build());
    });
  }

  private void upsertClassroomTeacher(Classroom classroom, User teacher) {
    if (classroomTeacherRepository.existsByClassroomIdAndUserId(classroom.getId(), teacher.getId())) {
      return;
    }
    classroomTeacherRepository.save(ClassroomTeacher.builder()
        .id(new ClassroomTeacherId(classroom.getId(), teacher.getId()))
        .classroom(classroom)
        .user(teacher)
        .isOwner(true)
        .build());
  }

  private Enrollment upsertEnrollment(Classroom classroom, User student) {
    return enrollmentRepository
        .findByClassroomIdAndUserIdAndLeftAtIsNull(classroom.getId(), student.getId())
        .orElseGet(() -> enrollmentRepository.save(Enrollment.builder()
            .classroom(classroom)
            .user(student)
            .build()));
  }

  private void unlockAllAvatarItems(User student) {
    Set<UUID> alreadyOwned = userInventoryItemRepository.findByUserId(student.getId()).stream()
        .map(i -> i.getAvatarItem().getId())
        .collect(Collectors.toSet());

    List<UserInventoryItem> toCreate = avatarItemRepository.findAll().stream()
        .filter(item -> !alreadyOwned.contains(item.getId()))
        .map(item -> UserInventoryItem.builder()
            .user(student)
            .avatarItem(item)
            .unlockedAt(LocalDateTime.now())
            .isEquipped(false)
            .build())
        .toList();

    if (!toCreate.isEmpty()) {
      userInventoryItemRepository.saveAll(toCreate);
    }
  }

  private void completeAllProgress(User student) {
    List<Stop> stops = stopRepository.findAllByOrderByDisplayOrderAsc();
    if (stops.isEmpty()) {
      throw new IllegalStateException(
          "TestUserSeeder: no stops found — GameSeeder must run before TestUserSeeder");
    }
    Stop lastStop = stops.get(stops.size() - 1);
    int lastLevelCount = Math.toIntExact(levelRepository.countByStopId(lastStop.getId()));

    UserProgress progress = userProgressRepository.findById(student.getId())
        .orElseGet(() -> {
          UserProgress p = new UserProgress();
          p.setUser(student);
          return p;
        });
    progress.setCurrentStop(lastStop);
    progress.setCurrentLevelOrder(Math.max(1, lastLevelCount));
    progress.setGameCompleted(true);
    progress.setLastProgressAt(Instant.now());
    userProgressRepository.save(progress);
  }

  private void awardAllMedals(User student) {
    List<Map.Entry<String, String>> medals = List.of(
        Map.entry("Rekruttert detektiv", "Lagde bruker"),
        Map.entry("Breaking news", "Du avslørte alle falske nyheter"),
        Map.entry("Fanget på kamera", "Du gjennomskuet bildene!"),
        Map.entry("Ikke klikk!", "Du gjennomskuet falske eposter"),
        Map.entry("Passordvokter", "Du bygde passord som holder skurker ute!"),
        Map.entry("God Handel", "Du var varsom i bazaaren"),
        Map.entry("Den sosiale møteplassen", "Du navigerte sosiale medier trygt!"),
        Map.entry("Hysj-hysj", "Hemmelig innpass, du kom deg inn i klasserommet"),
        Map.entry("Platinum", "Du fanget skurken!")
    );
    for (Map.Entry<String, String> medal : medals) {
      if (medalionRepository.existsByUserIdAndName(student.getId(), medal.getKey())) {
        continue;
      }
      medalionRepository.save(Medalion.builder()
          .name(medal.getKey())
          .description(medal.getValue())
          .user(student)
          .earned_at(LocalDateTime.now())
          .build());
    }
  }

  private void grantAllLevelNotes(User student) {
    List<LevelNoteTemplate> templates = levelNoteTemplateRepository.findAll();
    for (LevelNoteTemplate template : templates) {
      if (studentLevelNoteRepository.existsByUserAndTemplateId(student, template.getId())) {
        continue;
      }
      studentLevelNoteRepository.save(StudentLevelNote.builder()
          .user(student)
          .template(template)
          .category(template.getCategory())
          .content(template.getContent())
          .build());
    }
  }

  private void upsertWinningBossResult(Enrollment enrollment) {
    if (bossBattleResultRepository
        .findTopByEnrollmentIdOrderByCompletedAtDesc(enrollment.getId()).isPresent()) {
      return;
    }
    bossBattleResultRepository.save(BossBattleResult.builder()
        .enrollment(enrollment)
        .won(true)
        .timeSeconds(60)
        .playerHpRemaining(100)
        .completedAt(Instant.now())
        .build());
  }
}
