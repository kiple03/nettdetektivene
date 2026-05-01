package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.classroom.model.ClassroomCensorWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing censor words scoped to a specific classroom.
 */
@Repository
public interface ClassroomCensorWordRepository extends JpaRepository<ClassroomCensorWord, UUID> {

    /**
     * Returns all censor words belonging to the given classroom.
     *
     * @param classroomId the classroom ID
     * @return list of censor words for that classroom
     */
    List<ClassroomCensorWord> findByClassroomId(UUID classroomId);

    /**
     * Checks whether a specific word is already censored in the given classroom.
     *
     * @param classroomId the classroom ID
     * @param word        the word to check
     * @return {@code true} if the word is already registered as censored
     */
    boolean existsByClassroomIdAndWord(UUID classroomId, String word);

    /**
     * Removes all censor words associated with the given classroom.
     *
     * @param classroomId the classroom ID
     */
    void deleteByClassroomId(UUID classroomId);

    /**
     * Finds a censor word by its own ID and the owning classroom, ensuring ownership before modification.
     *
     * @param id          the censor-word record ID
     * @param classroomId the classroom ID
     * @return the matching entry, or empty if not found or not owned by that classroom
     */
    Optional<ClassroomCensorWord> findByIdAndClassroomId(UUID id, UUID classroomId);
}
