package org.ntnu.backend.notes.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for student-created personal notes.
 *
 * <p>This repository provides access to {@link Note} entities written by users
 * in their own notebook. It supports fetching all notes for a user and deleting
 * only notes that belong to that user.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    /**
     * Returns all personal notes for the given user, ordered by category.
     *
     * @param user the user whose notes should be fetched
     * @return the user's personal notes
     */
    List<Note> findAllByUserOrderByCategoryAsc(User user);

    /**
     * Deletes the note with the given id only if it belongs to the given user.
     *
     * @param id the id of the note to delete
     * @param user the owner of the note
     */
    void deleteByIdAndUser(UUID id, User user); // so users can't delete each other's notes
}