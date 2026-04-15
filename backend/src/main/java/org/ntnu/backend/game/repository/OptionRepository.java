package org.ntnu.backend.game.repository;

import org.ntnu.backend.game.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, Integer> {
    Optional<Option> findByIdAndQuestion_Level_Id(int optionId, int levelId);
}