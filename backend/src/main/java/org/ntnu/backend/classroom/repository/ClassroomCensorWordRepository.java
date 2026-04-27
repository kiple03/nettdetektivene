package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomCensorWordRepository extends JpaRepository<ClassroomCensorWord, UUID> {

    List<ClassroomCensorWord> findByClassroomId(UUID classroomId);

    boolean existsByClassroomIdAndWord(UUID classroomId, String word);

    void deleteByClassroomId(UUID classroomId);

    Optional<ClassroomCensorWord> findByIdAndClassroomId(UUID id, UUID classroomId);
}
