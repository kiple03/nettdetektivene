package org.ntnu.backend.notes.repository;

import java.util.List;
import java.util.Optional;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

  /**
   * Returns the first note template for the given level, if any.
   *
   * @param id the level id
   * @return the template, or empty if none exists
   */
  Optional<LevelNoteTemplate> findByLevelId(int id);

  /**
   * Returns all note templates for the given level, ordered by id ascending.
   *
   * @param id the level id
   * @return ordered list of templates
   */
  List<LevelNoteTemplate> findAllByLevelIdOrderByIdAsc(int id);
}