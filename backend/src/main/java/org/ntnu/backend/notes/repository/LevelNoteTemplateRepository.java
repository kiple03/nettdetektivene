package org.ntnu.backend.notes.repository;

import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for predefined note templates attached to levels.
 *
 * <p>This repository provides access to {@link LevelNoteTemplate} entities that
 * are configured for specific levels and later copied into student notebooks
 * when a level is completed.
 */
@Repository
public interface LevelNoteTemplateRepository extends JpaRepository<LevelNoteTemplate, Integer> {

    /**
     * Returns all note templates connected to the given level.
     *
     * @param levelId the id of the level
     * @return all note templates for the level
     */
    List<LevelNoteTemplate> findAllByLevelId(int levelId);
}