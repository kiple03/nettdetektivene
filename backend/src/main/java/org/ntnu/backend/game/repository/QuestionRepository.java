package org.ntnu.backend.game.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.game.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

  List<Question> findByLevelIdOrderByIdAsc(int levelId);

  Optional<Question> findByLevelIdAndQuestionText(int levelId, String questionText);

  /** Default (seeded) questions for a level — classroom_id IS NULL. */
  List<Question> findByLevelIdAndClassroomIdIsNull(int levelId);

  /**
   * Defaults + a specific classroom's teacher-added questions for a level.
   * Used for student-side question fetch.
   */
  @Query("SELECT q FROM Question q WHERE q.level.id = :levelId AND (q.classroomId IS NULL OR q.classroomId = :classroomId)")
  List<Question> findByLevelIdForClassroom(@Param("levelId") int levelId, @Param("classroomId") UUID classroomId);

  /** Only teacher-added questions for a specific classroom and level. */
  List<Question> findByLevelIdAndClassroomId(int levelId, UUID classroomId);
}
