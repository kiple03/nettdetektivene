package org.ntnu.backend.game.repository;

import org.ntnu.backend.game.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
