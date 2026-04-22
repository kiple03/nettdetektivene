package org.ntnu.backend.notes.repository;

import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentLevelNoteRepository extends JpaRepository<StudentLevelNote, UUID> {

    List<StudentLevelNote> findAllByUserOrderByCategoryAsc(User user);

    boolean existsByUserAndTemplateId(User user, int templateId);
}