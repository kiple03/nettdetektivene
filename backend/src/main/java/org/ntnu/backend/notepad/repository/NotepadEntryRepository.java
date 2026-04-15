package org.ntnu.backend.notepad.repository;

import java.util.UUID;
import org.ntnu.backend.notepad.model.NotepadEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotepadEntryRepository extends JpaRepository<NotepadEntry, UUID> {
}
