package org.ntnu.backend.notes.repository;

import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelNoteTemplateRepository extends JpaRepository<LevelNoteTemplate, Integer> {

    List<LevelNoteTemplate> findAllByLevelId(int levelId);
}