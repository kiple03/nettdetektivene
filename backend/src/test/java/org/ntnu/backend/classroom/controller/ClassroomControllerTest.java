package org.ntnu.backend.classroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.JoinClassroomRequest;
import org.ntnu.backend.classroom.service.ClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClassroomControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ClassroomService classroomService;
    private JwtAuthenticatedPrincipal principal;

    private UUID classroomId;
    private UUID userId;
    private ClassroomResponse classroomResponse;

    @BeforeEach
    void setUp() {
        classroomService = mock(ClassroomService.class);
        principal = mock(JwtAuthenticatedPrincipal.class);

        ClassroomController classroomController = new ClassroomController(classroomService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(classroomController)
                .setValidator(validator)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(JwtAuthenticatedPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return principal;
                    }
                })
                .build();

        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        classroomResponse = ClassroomResponse.builder()
                .id(classroomId.toString())
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .createdAt(LocalDateTime.of(2026, 4, 28, 12, 30))
                .build();
    }

    @Test
    void getClassroom_shouldReturnClassroom() throws Exception {
        when(classroomService.getClassroom(classroomId)).thenReturn(classroomResponse);

        mockMvc.perform(get("/classrooms/{id}", classroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId.toString()))
                .andExpect(jsonPath("$.title").value("Klasse 5A"))
                .andExpect(jsonPath("$.description").value("Test classroom"))
                .andExpect(jsonPath("$.joinCode").value("ABC123"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-28T12:30:00"));

        verify(classroomService).getClassroom(classroomId);
    }

    @Test
    void getMyClassroom_whenStudentHasClassroom_shouldReturnClassroom() throws Exception {
        when(principal.getUserId()).thenReturn(userId);
        when(classroomService.getMyClassroom(userId)).thenReturn(Optional.of(classroomResponse));

        mockMvc.perform(get("/classrooms/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId.toString()))
                .andExpect(jsonPath("$.title").value("Klasse 5A"))
                .andExpect(jsonPath("$.description").value("Test classroom"))
                .andExpect(jsonPath("$.joinCode").value("ABC123"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-28T12:30:00"));

        verify(classroomService).getMyClassroom(userId);
    }

    @Test
    void getMyClassroom_whenStudentHasNoClassroom_shouldReturnNoContent() throws Exception {
        when(principal.getUserId()).thenReturn(userId);
        when(classroomService.getMyClassroom(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/classrooms/my"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(classroomService).getMyClassroom(userId);
    }

    @Test
    void joinClassroom_shouldReturnJoinedClassroom() throws Exception {
        when(principal.getUserId()).thenReturn(userId);

        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(classroomService.joinClassroom(any(JoinClassroomRequest.class), eq(userId)))
                .thenAnswer(invocation -> classroomResponse);

        mockMvc.perform(post("/classrooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId.toString()))
                .andExpect(jsonPath("$.title").value("Klasse 5A"))
                .andExpect(jsonPath("$.description").value("Test classroom"))
                .andExpect(jsonPath("$.joinCode").value("ABC123"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-28T12:30:00"));

        verify(classroomService).joinClassroom(any(JoinClassroomRequest.class), eq(userId));
    }

    @Test
    void joinClassroom_withBlankJoinCode_shouldReturnBadRequestAndNotCallService() throws Exception {
        JoinClassroomRequest request = new JoinClassroomRequest("");

        mockMvc.perform(post("/classrooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(classroomService, never()).joinClassroom(any(), any());
    }

    @Test
    void joinClassroom_withTooShortJoinCode_shouldReturnBadRequestAndNotCallService() throws Exception {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC");

        mockMvc.perform(post("/classrooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(classroomService, never()).joinClassroom(any(), any());
    }

    @Test
    void joinClassroom_withTooLongJoinCode_shouldReturnBadRequestAndNotCallService() throws Exception {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC1234");

        mockMvc.perform(post("/classrooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(classroomService, never()).joinClassroom(any(), any());
    }
}