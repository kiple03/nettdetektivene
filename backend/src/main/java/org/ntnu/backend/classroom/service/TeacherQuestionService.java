package org.ntnu.backend.classroom.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.TeacherMultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.TeacherQuestionResponse;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherQuestionService {

    private final QuestionRepository questionRepository;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final OptionParser optionParser;

    /**
     * Returns all questions for a level visible to the teacher: default questions
     * (is_default=true) plus this classroom's teacher-added questions.
     */
    public List<TeacherQuestionResponse> listQuestionsForTeacher(
            UUID classroomId, int levelId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        List<Question> questions = questionRepository.findByLevelIdForClassroom(levelId, classroomId);
        return questions.stream()
            .map(this::toTeacherResponse)
            .toList();
    }

    private TeacherQuestionResponse toTeacherResponse(Question question) {
        LevelType type = question.getLevel().getLevelType();
        List<TeacherMultipleChoiceOptionResponse> options = type == LevelType.MULTIPLE_CHOICE
            ? toMcOptions(optionParser.parse(question.getOptionContent(), type))
            : List.of();
        return new TeacherQuestionResponse(
            question.getId(),
            type,
            question.getQuestionText(),
            options,
            question.isDefault(),
            question.getClassroomId()
        );
    }

    private List<TeacherMultipleChoiceOptionResponse> toMcOptions(List<Option> options) {
        return options.stream()
            .map(opt -> {
                MultipleChoiceOption mc = (MultipleChoiceOption) opt;
                return new TeacherMultipleChoiceOptionResponse(
                    mc.getId(),
                    mc.getText(),
                    mc.getImageUrl(),
                    mc.isCorrect(),
                    mc.getExplanation()
                );
            })
            .toList();
    }
}
