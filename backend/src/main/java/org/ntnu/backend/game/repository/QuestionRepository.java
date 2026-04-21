package org.ntnu.backend.game.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

  List<Question> findByLevelIdOrderByIdAsc(int levelId);
}
