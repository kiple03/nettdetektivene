package org.ntnu.backend.notes.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.notes.model.Note;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findAllByUserOrderByCategoryAsc(User user);

    void deleteByIdAndUser(UUID id, User user); // so users can't delete each other's notes
}