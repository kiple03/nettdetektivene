package org.ntnu.backend.classroom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.CreateClassifyItemsItemRequest;
import org.ntnu.backend.classroom.dto.CreateClassifyItemsQuestionRequest;
import org.ntnu.backend.classroom.dto.CreateMultipleChoiceOptionRequest;
import org.ntnu.backend.classroom.dto.CreateMultipleChoiceQuestionRequest;
import org.ntnu.backend.classroom.dto.CreatePasswordStrengthQuestionRequest;
import org.ntnu.backend.classroom.dto.CreatePictureQuizOptionRequest;
import org.ntnu.backend.classroom.dto.CreatePictureQuizQuestionRequest;
import org.ntnu.backend.classroom.dto.CreateTeacherQuestionRequest;
import org.ntnu.backend.classroom.dto.CreateTextQuizOptionRequest;
import org.ntnu.backend.classroom.dto.CreateTextQuizQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdateClassifyItemsQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdateMultipleChoiceQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdatePasswordStrengthQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdatePictureQuizQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdateTeacherQuestionRequest;
import org.ntnu.backend.classroom.dto.UpdateTextQuizQuestionRequest;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.teacher.TeacherClassifyItemsOptionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherMultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherOptionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherPasswordStrengthOptionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherPictureQuizOptionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherQuestionResponse;
import org.ntnu.backend.game.dto.teacher.TeacherTextQuizOptionResponse;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.PictureQuizOption;
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

/**
 * Service for teacher-facing CRUD operations on classroom questions.
 * Handles creation, update, and deletion of teacher-added (non-default) questions
 * across all supported level types.
 */
@Service
@RequiredArgsConstructor
public class TeacherQuestionService {

    private static final Set<LevelType> SUPPORTED_TYPES = Set.of(LevelType.MULTIPLE_CHOICE, LevelType.PICTURE_QUIZ, LevelType.CLASSIFY_ITEMS, LevelType.PASSWORD_STRENGTH, LevelType.TEXT_QUIZ);

    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final OptionParser optionParser;
    private final OptionValidator optionValidator;
    private final ObjectMapper objectMapper;

    /**
     * Returns all questions for a given level in a classroom, formatted for teacher view.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level ID
     * @param principal   the authenticated teacher
     * @return list of teacher question responses
     * @throws org.springframework.security.access.AccessDeniedException if teacher lacks access to the classroom
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
     * Creates a new teacher-added question for the given level.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level ID
     * @param request     typed create request (must match the level's type)
     * @param principal   the authenticated teacher
     * @return the saved question response
     * @throws LevelNotFoundException     if the level does not exist
     * @throws IllegalArgumentException   if the level type is unsupported or the request type mismatches
     * @throws org.springframework.security.access.AccessDeniedException if teacher lacks access
     */
    public TeacherQuestionResponse createQuestion(
            UUID classroomId, int levelId,
            CreateTeacherQuestionRequest request,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        Level level = levelRepository.findById(levelId)
            .orElseThrow(() -> new LevelNotFoundException(levelId));

        LevelType type = level.getLevelType();
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                "Level type " + type + " does not support teacher-added questions");
        }

        List<Option> options = switch (request) {
            case CreateMultipleChoiceQuestionRequest mc -> buildMcOptions(mc.options());
            case CreatePictureQuizQuestionRequest pq -> buildPqOptions(pq.options());
            case CreateClassifyItemsQuestionRequest ci -> buildCiOptions(ci.labels(), ci.items());
            case CreatePasswordStrengthQuestionRequest ps -> buildPsOptions(ps);
            case CreateTextQuizQuestionRequest tq -> buildTqOptions(tq.options());
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass().getSimpleName());
        };

        optionValidator.validate(options, type);

        String optionContent = serialize(options);

        Question question = new Question();
        question.setQuestionText(request.questionText());
        question.setOptionContent(optionContent);
        question.setLevel(level);
        question.setDefault(false);
        question.setClassroomId(classroomId);

        return toTeacherResponse(questionRepository.save(question));
    }

    /**
     * Updates an existing teacher-added question.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level ID the question belongs to
     * @param questionId  the question ID to update
     * @param request     typed update request matching the level's type
     * @param principal   the authenticated teacher
     * @return the updated question response
     * @throws QuestionNotFoundException  if the question does not exist or does not belong to the level
     * @throws IllegalArgumentException   if attempting to edit a default (seeded) question or mismatched type
     * @throws org.springframework.security.access.AccessDeniedException if the question belongs to a different classroom
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

        LevelType type = question.getLevel().getLevelType();

        List<Option> options = switch (request) {
            case UpdateMultipleChoiceQuestionRequest mc -> buildMcOptions(mc.options());
            case UpdatePictureQuizQuestionRequest pq -> buildPqOptions(pq.options());
            case UpdateClassifyItemsQuestionRequest ci -> buildCiOptions(ci.labels(), ci.items());
            case UpdatePasswordStrengthQuestionRequest ps -> buildPsOptions(ps);
            case UpdateTextQuizQuestionRequest tq -> buildTqOptions(tq.options());
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass().getSimpleName());
        };

        optionValidator.validate(options, type);

        question.setQuestionText(request.questionText());
        question.setOptionContent(serialize(options));

        return toTeacherResponse(questionRepository.save(question));
    }

    /**
     * Deletes a teacher-added question.
     *
     * @param classroomId the classroom UUID
     * @param levelId     the level ID the question belongs to
     * @param questionId  the question ID to delete
     * @param principal   the authenticated teacher
     * @throws QuestionNotFoundException  if the question does not exist or does not belong to the level
     * @throws IllegalArgumentException   if attempting to delete a default (seeded) question
     * @throws org.springframework.security.access.AccessDeniedException if the question belongs to a different classroom
     */
    public void deleteQuestion(
            UUID classroomId, int levelId, int questionId,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new QuestionNotFoundException(questionId));

        if (question.getLevel().getId() != levelId) {
            throw new QuestionNotFoundException(questionId);
        }
        if (question.isDefault()) {
            throw new IllegalArgumentException("Cannot delete a default (seeded) question");
        }
        if (!classroomId.equals(question.getClassroomId())) {
            throw new AccessDeniedException("Question does not belong to this classroom");
        }

        questionRepository.delete(question);
    }

    private List<Option> buildMcOptions(List<CreateMultipleChoiceOptionRequest> dtos) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            CreateMultipleChoiceOptionRequest dto = dtos.get(i);
            options.add(new MultipleChoiceOption(i + 1, dto.text(), dto.imageUrl(), dto.responseImageUrl(), dto.isCorrect(), dto.explanation()));
        }
        return options;
    }

    private List<Option> buildPqOptions(List<CreatePictureQuizOptionRequest> dtos) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            CreatePictureQuizOptionRequest dto = dtos.get(i);
            options.add(new PictureQuizOption(i + 1, dto.text(), dto.imageUrl(), dto.isCorrect(), dto.explanation()));
        }
        return options;
    }

    private List<Option> buildCiOptions(List<String> labels, List<CreateClassifyItemsItemRequest> dtos) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            CreateClassifyItemsItemRequest dto = dtos.get(i);
            options.add(new ClassifyItemsOption("item-" + (i + 1), dto.text(), labels, dto.correctLabel(), dto.explanation()));
        }
        return options;
    }

    private List<Option> buildPsOptions(CreatePasswordStrengthQuestionRequest ps) {
        return List.of(new PasswordStrengthOption(
            1, ps.minLength(), ps.requireDigit(), ps.requireUpper(),
            ps.requireLower(), ps.requireSymbol(), null, ps.successFeedback(), ps.failureFeedback()));
    }

    private List<Option> buildTqOptions(List<CreateTextQuizOptionRequest> dtos) {
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            CreateTextQuizOptionRequest dto = dtos.get(i);
            options.add(new MultipleChoiceOption(i + 1, dto.text(), null, dto.isCorrect(), dto.explanation()));
        }
        return options;
    }

    private List<Option> buildPsOptions(UpdatePasswordStrengthQuestionRequest ps) {
        return List.of(new PasswordStrengthOption(
            1, ps.minLength(), ps.requireDigit(), ps.requireUpper(),
            ps.requireLower(), ps.requireSymbol(), null, ps.successFeedback(), ps.failureFeedback()));
    }

    private String serialize(List<Option> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize responseUrls", e);
        }
    }

    private TeacherQuestionResponse toTeacherResponse(Question question) {
        LevelType type = question.getLevel().getLevelType();
        List<TeacherOptionResponse> options = switch (type) {
            case MULTIPLE_CHOICE    -> toMcOptions(optionParser.parse(question.getOptionContent(), type));
            case PICTURE_QUIZ       -> toPqOptions(optionParser.parse(question.getOptionContent(), type));
            case CLASSIFY_ITEMS     -> toCiOptions(optionParser.parse(question.getOptionContent(), type));
            case PASSWORD_STRENGTH  -> toPsOptions(optionParser.parse(question.getOptionContent(), type));
            case TEXT_QUIZ          -> toTqOptions(optionParser.parse(question.getOptionContent(), type));
            default -> List.of();
        };
        return new TeacherQuestionResponse(
            question.getId(),
            type,
            question.getQuestionText(),
            options,
            question.isDefault(),
            question.getClassroomId()
        );
    }

    private List<TeacherOptionResponse> toMcOptions(List<Option> options) {
        return options.stream()
            .<TeacherOptionResponse>map(opt -> {
                MultipleChoiceOption mc = (MultipleChoiceOption) opt;
                return new TeacherMultipleChoiceOptionResponse(
                    mc.getId(),
                    mc.getText(),
                    mc.getImageUrl(),
                    mc.getResponseImageUrl(),
                    mc.isCorrect(),
                    mc.getExplanation()
                );
            })
            .toList();
    }

    private List<TeacherOptionResponse> toPqOptions(List<Option> options) {
        return options.stream()
            .<TeacherOptionResponse>map(opt -> {
                PictureQuizOption pq = (PictureQuizOption) opt;
                return new TeacherPictureQuizOptionResponse(
                    pq.id(),
                    pq.text(),
                    pq.imageUrl(),
                    pq.isCorrect(),
                    pq.explanation()
                );
            })
            .toList();
    }

    private List<TeacherOptionResponse> toCiOptions(List<Option> options) {
        return options.stream()
            .<TeacherOptionResponse>map(opt -> {
                ClassifyItemsOption ci = (ClassifyItemsOption) opt;
                return new TeacherClassifyItemsOptionResponse(
                    ci.getKey(),
                    ci.getText(),
                    ci.getLabels(),
                    ci.getCorrectLabel(),
                    ci.getExplanation()
                );
            })
            .toList();
    }

    private List<TeacherOptionResponse> toTqOptions(List<Option> options) {
        return options.stream()
            .<TeacherOptionResponse>map(opt -> {
                MultipleChoiceOption mc = (MultipleChoiceOption) opt;
                return new TeacherTextQuizOptionResponse(
                    mc.getId(),
                    mc.getText(),
                    mc.isCorrect(),
                    mc.getExplanation()
                );
            })
            .toList();
    }

    private List<TeacherOptionResponse> toPsOptions(List<Option> options) {
        PasswordStrengthOption ps = (PasswordStrengthOption) options.get(0);
        return List.of(new TeacherPasswordStrengthOptionResponse(
            ps.getId(),
            ps.getMinLength(),
            ps.isRequireDigit(),
            ps.isRequireUpper(),
            ps.isRequireLower(),
            ps.isRequireSymbol(),
            ps.getSuccessFeedback(),
            ps.getFailureFeedback()
        ));
    }
}
