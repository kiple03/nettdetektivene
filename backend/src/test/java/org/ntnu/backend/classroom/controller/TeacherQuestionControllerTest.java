package org.ntnu.backend.classroom.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.service.TeacherQuestionService;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.game.dto.TeacherMultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.TeacherQuestionResponse;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeacherQuestionController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class TeacherQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherQuestionService teacherQuestionService;

    @MockitoBean
    private JwtService jwtService;

    private JwtAuthenticationToken teacherAuth(UUID userId) {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher@test.local", userId, RoleEnum.TEACHER, null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    private JwtAuthenticationToken studentAuth() {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student@test.local", UUID.randomUUID(), RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    @Test
    void listQuestions_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        int levelId = 1;

        TeacherMultipleChoiceOptionResponse opt = new TeacherMultipleChoiceOptionResponse(
            1, "Paris", null, true, "Paris is the capital");
        TeacherQuestionResponse question = new TeacherQuestionResponse(
            42, LevelType.MULTIPLE_CHOICE, "What is the capital of France?",
            List.of(opt), true, null);

        when(teacherQuestionService.listQuestionsForTeacher(eq(classroomId), eq(levelId), any()))
            .thenReturn(List.of(question));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/levels/" + levelId + "/questions")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(42))
            .andExpect(jsonPath("$[0].questionText").value("What is the capital of France?"))
            .andExpect(jsonPath("$[0].levelType").value("MULTIPLE_CHOICE"))
            .andExpect(jsonPath("$[0].isDefault").value(true))
            .andExpect(jsonPath("$[0].classroomId").doesNotExist())
            .andExpect(jsonPath("$[0].options[0].text").value("Paris"))
            .andExpect(jsonPath("$[0].options[0].isCorrect").value(true));
    }

    @Test
    void listQuestions_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/levels/1/questions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listQuestions_studentRole_returns403() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/levels/1/questions")
                .with(authentication(studentAuth())))
            .andExpect(status().isForbidden());
    }

    @Test
    void listQuestions_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherQuestionService.listQuestionsForTeacher(eq(classroomId), eq(1), any()))
            .thenThrow(new AccessDeniedException("You do not have permission to access this classroom"));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/levels/1/questions")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void listQuestions_emptyLevel_returnsEmptyArray() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherQuestionService.listQuestionsForTeacher(eq(classroomId), eq(99), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/levels/99/questions")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createQuestion_returnsCreated() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        int levelId = 1;

        TeacherMultipleChoiceOptionResponse opt = new TeacherMultipleChoiceOptionResponse(
            1, "Paris", null, true, "Paris is the capital");
        TeacherQuestionResponse created = new TeacherQuestionResponse(
            99, LevelType.MULTIPLE_CHOICE, "What is the capital of France?",
            List.of(opt), false, classroomId);

        when(teacherQuestionService.createQuestion(eq(classroomId), eq(levelId), any(), any()))
            .thenReturn(created);

        String body = """
            {
              "questionText": "What is the capital of France?",
              "levelType": "MULTIPLE_CHOICE",
              "options": [
                {"text": "Paris", "imageUrl": null, "isCorrect": true, "explanation": "Paris is the capital"},
                {"text": "Berlin", "imageUrl": null, "isCorrect": false, "explanation": "Berlin is Germany's capital"}
              ]
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + classroomId + "/levels/" + levelId + "/questions")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.questionText").value("What is the capital of France?"))
            .andExpect(jsonPath("$.isDefault").value(false))
            .andExpect(jsonPath("$.options[0].text").value("Paris"));
    }

    @Test
    void createQuestion_missingQuestionText_returns400() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        String body = """
            {
              "levelType": "MULTIPLE_CHOICE",
              "options": [
                {"text": "Paris", "isCorrect": true, "explanation": "capital"}
              ]
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + classroomId + "/levels/1/questions")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestion_levelNotFound_returns404() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherQuestionService.createQuestion(eq(classroomId), eq(999), any(), any()))
            .thenThrow(new LevelNotFoundException(999));

        String body = """
            {
              "questionText": "Q?",
              "levelType": "MULTIPLE_CHOICE",
              "options": [
                {"text": "A", "isCorrect": true, "explanation": "yes"}
              ]
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + classroomId + "/levels/999/questions")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound());
    }

    @Test
    void createQuestion_nonMcLevel_returns400() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherQuestionService.createQuestion(eq(classroomId), eq(2), any(), any()))
            .thenThrow(new IllegalArgumentException("Only MULTIPLE_CHOICE levels support teacher-added questions"));

        String body = """
            {
              "questionText": "Q?",
              "levelType": "MULTIPLE_CHOICE",
              "options": [
                {"text": "A", "isCorrect": true, "explanation": "yes"}
              ]
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + classroomId + "/levels/2/questions")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestion_emptyOptions_returns400() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        String body = """
            {
              "questionText": "Q?",
              "levelType": "MULTIPLE_CHOICE",
              "options": []
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + classroomId + "/levels/1/questions")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestion_noAuth_returns401() throws Exception {
        String body = """
            {"questionText": "Q?", "levelType": "MULTIPLE_CHOICE", "options": []}
            """;

        mockMvc.perform(post("/teacher/classrooms/" + UUID.randomUUID() + "/levels/1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createQuestion_studentRole_returns403() throws Exception {
        String body = """
            {
              "questionText": "Q?",
              "levelType": "MULTIPLE_CHOICE",
              "options": [{"text": "A", "isCorrect": true, "explanation": "yes"}]
            }
            """;

        mockMvc.perform(post("/teacher/classrooms/" + UUID.randomUUID() + "/levels/1/questions")
                .with(authentication(studentAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }
}
