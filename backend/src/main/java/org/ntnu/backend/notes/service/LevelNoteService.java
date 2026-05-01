package org.ntnu.backend.notes.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.notes.model.LevelNoteTemplate;
import org.ntnu.backend.notes.model.StudentLevelNote;
import org.ntnu.backend.notes.repository.LevelNoteTemplateRepository;
import org.ntnu.backend.notes.repository.StudentLevelNoteRepository;
import org.ntnu.backend.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grants preset notes to a student when they complete a level.
 *
 * <p>Call {@link #grantLevelNotes(User, Level)} from wherever level completion
 * is recorded (e.g. alongside saving a {@code LevelCompletion}).
 *
 * <p>The method is idempotent — if the student has already received a
 * template's note (checked via the unique constraint), it is silently skipped.
 * This means replaying a level never produces duplicate notes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LevelNoteService {

    private final LevelNoteTemplateRepository templateRepository;
    private final StudentLevelNoteRepository studentLevelNoteRepository;

    /**
     * Copies all note templates for the given level into the student's notebook,
     * skipping any that have already been granted.
     *
     * @param user  the student who completed the level
     * @param level the level that was completed
     */
    public void grantLevelNotes(User user, Level level) {
        List<LevelNoteTemplate> templates = templateRepository.findAllByLevelId(level.getId());

        for (LevelNoteTemplate template : templates) {
            boolean alreadyGranted = studentLevelNoteRepository.existsByUserAndTemplateId(user, template.getId());
            if (alreadyGranted) continue;

            StudentLevelNote note = StudentLevelNote.builder()
                    .user(user)
                    .template(template)
                    .category(template.getCategory())
                    .content(template.getContent())
                    .build();

            studentLevelNoteRepository.save(note);
        }
    }
}