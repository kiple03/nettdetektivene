package org.ntnu.backend.classroom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.CreateMultipleChoiceOptionRequest;
import org.ntnu.backend.classroom.dto.CreateTeacherQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdateTeacherQuestionRequest;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.TeacherMultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.TeacherQuestionResponse;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class TeacherQuestionService {

    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final OptionParser optionParser;
    private final OptionValidator optionValidator;
    private final ObjectMapper objectMapper;

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

    /**
     * Creates a teacher-added question for a classroom level.
     * Only {@link LevelType#MULTIPLE_CHOICE} levels are supported this iteration.
     */
    public TeacherQuestionResponse createQuestion(
            UUID classroomId, int levelId,
            CreateTeacherQuestionRequest request,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        Level level = levelRepository.findById(levelId)
            .orElseThrow(() -> new LevelNotFoundException(levelId));

        if (level.getLevelType() != LevelType.MULTIPLE_CHOICE) {
            throw new IllegalArgumentException(
                "Only MULTIPLE_CHOICE levels support teacher-added questions");
        }

        if (request.levelType() != level.getLevelType()) {
            throw new IllegalArgumentException(
                "Request levelType " + request.levelType() + " does not match level type " + level.getLevelType());
        }

        List<Option> options = buildMcOptions(request.options());
        optionValidator.validate(options, LevelType.MULTIPLE_CHOICE);

        String optionContent;
        try {
            optionContent = objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize options", e);
        }

        Question question = new Question();
        question.setQuestionText(request.questionText());
        question.setOptionContent(optionContent);
        question.setLevel(level);
        question.setDefault(false);
        question.setClassroomId(classroomId);

        return toTeacherResponse(questionRepository.save(question));
    }

    /**
     * Updates a teacher-added question. Rejects default questions (400) and
     * questions not belonging to the given classroom (403).
     */
    public TeacherQuestionResponse updateQuestion(
            UUID classroomId, int levelId, int questionId,
            UpdateTeacherQuestionRequest request,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

        if (question.getLevel().getId() != levelId) {
            throw new QuestionNotFoundException(questionId);
        }

        if (question.isDefault()) {
            throw new IllegalArgumentException("Cannot edit a default (seeded) question");
        }

        if (!classroomId.equals(question.getClassroomId())) {
            throw new AccessDeniedException("Question does not belong to this classroom");
        }

        List<Option> options = buildMcOptions(request.options());
        optionValidator.validate(options, LevelType.MULTIPLE_CHOICE);

        String optionContent;
        try {
            optionContent = objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize options", e);
        }

        question.setQuestionText(request.questionText());
        question.setOptionContent(optionContent);

        return toTeacherResponse(questionRepository.save(question));
    }

    private List<Option> buildMcOptions(List<CreateMultipleChoiceOptionRequest> dtos) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            CreateMultipleChoiceOptionRequest dto = dtos.get(i);
            options.add(new MultipleChoiceOption(i + 1, dto.text(), dto.imageUrl(), dto.isCorrect(), dto.explanation()));
        }
        return options;
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
