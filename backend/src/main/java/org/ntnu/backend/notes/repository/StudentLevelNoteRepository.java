package org.ntnu.backend.notes.repository;

import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for granted level notes belonging to students.
 *
 * <p>This repository provides access to {@link StudentLevelNote} entities that
 * are copied from level note templates when a student completes a level.
 * It supports listing all granted notes for a user and checking whether a
 * specific template has already been granted.
 */
@Repository
public interface StudentLevelNoteRepository extends JpaRepository<StudentLevelNote, UUID> {

    /**
     * Returns all granted level notes for the given user, ordered by category.
     *
     * @param user the user whose granted notes should be fetched
     * @return the user's granted level notes
     */
    List<StudentLevelNote> findAllByUserOrderByCategoryAsc(User user);

    /**
     * Checks whether the given user has already received a granted note copied
     * from the specified template.
     *
     * @param user the user to check
     * @param templateId the id of the level note template
     * @return {@code true} if the note has already been granted, otherwise {@code false}
     */
    boolean existsByUserAndTemplateId(User user, int templateId);
}