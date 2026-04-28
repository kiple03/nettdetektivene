package org.ntnu.backend.classroom.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.service.CensorWordService;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherClassroomController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class TeacherClassroomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherClassroomService teacherClassroomService;

    @MockitoBean
    private CensorWordService censorWordService;

    @MockitoBean
    private JwtService jwtService;


    private JwtAuthenticationToken teacherAuth(UUID userId) {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", userId, RoleEnum.TEACHER, null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    @Test
    void listClassrooms_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        ClassroomSummaryResponse summary = ClassroomSummaryResponse.builder()
            .id(classroomId.toString())
            .title("Math 101")
            .description("Basic math")
            .joinCode("ABC123")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .studentCount(5)
            .build();

        when(teacherClassroomService.listClassrooms(teacherId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/teacher/classrooms")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(classroomId.toString()))
            .andExpect(jsonPath("$[0].title").value("Math 101"))
            .andExpect(jsonPath("$[0].studentCount").value(5));
    }

    @Test
    void listClassrooms_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listClassrooms_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/classrooms")
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createClassroom_returnsCreated() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        ClassroomResponse response = ClassroomResponse.builder()
            .id(classroomId.toString())
            .title("Science 101")
            .description("Basic science")
            .joinCode("XYZ789")
            .build();

        when(teacherClassroomService.createClassroom(any(CreateClassroomRequest.class), eq(teacherId)))
            .thenReturn(response);

        mockMvc.perform(post("/teacher/classrooms")
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Science 101\",\"description\":\"Basic science\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(classroomId.toString()))
            .andExpect(jsonPath("$.title").value("Science 101"))
            .andExpect(jsonPath("$.joinCode").value("XYZ789"));
    }

    @Test
    void createClassroom_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/teacher/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Science 101\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createClassroom_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(post("/teacher/classrooms")
                .with(authentication(studentAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Science 101\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getClassroom_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        ClassroomResponse response = ClassroomResponse.builder()
            .id(classroomId.toString())
            .title("Math 101")
            .description("Basic math")
            .joinCode("ABC123")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .build();

        when(teacherClassroomService.getClassroom(eq(classroomId), any())).thenReturn(response);

        mockMvc.perform(get("/teacher/classrooms/" + classroomId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(classroomId.toString()))
            .andExpect(jsonPath("$.title").value("Math 101"))
            .andExpect(jsonPath("$.joinCode").value("ABC123"));
    }

    @Test
    void getClassroom_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getClassroom_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID())
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void getClassroom_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherClassroomService.getClassroom(eq(classroomId), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateClassroom_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        ClassroomResponse response = ClassroomResponse.builder()
            .id(classroomId.toString())
            .title("Updated Title")
            .description("Updated desc")
            .joinCode("ABC123")
            .build();

        when(teacherClassroomService.updateClassroom(eq(classroomId), any(UpdateClassroomRequest.class), any()))
            .thenReturn(response);

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\",\"description\":\"Updated desc\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(classroomId.toString()))
            .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateClassroom_noAuth_returns401() throws Exception {
        mockMvc.perform(patch("/teacher/classrooms/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateClassroom_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(patch("/teacher/classrooms/" + UUID.randomUUID())
                .with(authentication(studentAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void listStudents_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        UUID studentId = UUID.randomUUID();
        StudentProgressResponse student = StudentProgressResponse.builder()
            .userId(studentId.toString())
            .username("alice")
            .email("alice@example.com")
            .currentStopDisplayOrder(3)
            .medalsEarned(3)
            .build();

        when(teacherClassroomService.listStudents(eq(classroomId), any())).thenReturn(List.of(student));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/students")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value(studentId.toString()))
            .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
            .andExpect(jsonPath("$[0].currentStopDisplayOrder").value(3))
            .andExpect(jsonPath("$[0].medalsEarned").value(3));
    }

    @Test
    void listStudents_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/students"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listStudents_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/students")
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void listStudents_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherClassroomService.listStudents(eq(classroomId), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/students")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void getStudentDetail_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        StudentDetailResponse detail = StudentDetailResponse.builder()
            .username("alice")
            .currentStopName("Fotografen")
            .currentLevelOrder(2)
            .totalLevelsCompleted(4)
            .gameCompleted(false)
            .build();

        when(teacherClassroomService.getStudentDetail(eq(classroomId), eq(studentId), any()))
            .thenReturn(detail);

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/students/" + studentId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.currentStopName").value("Fotografen"))
            .andExpect(jsonPath("$.currentLevelOrder").value(2))
            .andExpect(jsonPath("$.totalLevelsCompleted").value(4))
            .andExpect(jsonPath("$.gameCompleted").value(false));
    }

    @Test
    void getStudentDetail_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/students/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getStudentDetail_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/students/" + UUID.randomUUID())
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void getStudentDetail_notEnrolled_returns404() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        when(teacherClassroomService.getStudentDetail(eq(classroomId), eq(studentId), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/students/" + studentId)
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isNotFound());
    }

    @Test
    void listMysterySubmissions_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        Instant submittedAt = Instant.parse("2025-03-01T10:00:00Z");

        MysterySubmissionResponse submission = MysterySubmissionResponse.builder()
            .id(submissionId)
            .studentUsername("alice")
            .content("My mystery answer")
            .imageUrl(null)
            .submittedAt(submittedAt)
            .status(MysteryStatusEnum.PENDING)
            .featured(false)
            .build();

        when(teacherClassroomService.listMysterySubmissions(eq(classroomId), any())).thenReturn(List.of(submission));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/mystery")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(submissionId.toString()))
            .andExpect(jsonPath("$[0].studentUsername").value("alice"))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[0].featured").value(false));
    }

    @Test
    void listMysterySubmissions_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/mystery"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listMysterySubmissions_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/classrooms/" + UUID.randomUUID() + "/mystery")
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }

    @Test
    void listMysterySubmissions_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherClassroomService.listMysterySubmissions(eq(classroomId), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/teacher/classrooms/" + classroomId + "/mystery")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateClassroom_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();

        when(teacherClassroomService.updateClassroom(eq(classroomId), any(UpdateClassroomRequest.class), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void reviewMystery_approveSubmission_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        Instant submittedAt = Instant.parse("2025-03-01T10:00:00Z");

        MysterySubmissionResponse response = MysterySubmissionResponse.builder()
            .id(submissionId)
            .studentUsername("alice")
            .content("My mystery answer")
            .imageUrl(null)
            .submittedAt(submittedAt)
            .status(MysteryStatusEnum.APPROVED)
            .featured(false)
            .build();

        when(teacherClassroomService.reviewMysterySubmission(eq(classroomId), eq(submissionId), any(), any()))
            .thenReturn(response);

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId + "/mystery/" + submissionId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(submissionId.toString()))
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.featured").value(false));
    }

    @Test
    void reviewMystery_featureSubmission_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        Instant submittedAt = Instant.parse("2025-03-01T10:00:00Z");

        MysterySubmissionResponse response = MysterySubmissionResponse.builder()
            .id(submissionId)
            .studentUsername("alice")
            .content("My mystery answer")
            .imageUrl(null)
            .submittedAt(submittedAt)
            .status(MysteryStatusEnum.PENDING)
            .featured(true)
            .build();

        when(teacherClassroomService.reviewMysterySubmission(eq(classroomId), eq(submissionId), any(), any()))
            .thenReturn(response);

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId + "/mystery/" + submissionId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"featured\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.featured").value(true))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void reviewMystery_noAuth_returns401() throws Exception {
        mockMvc.perform(patch("/teacher/classrooms/" + UUID.randomUUID() + "/mystery/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void reviewMystery_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, RoleEnum.STUDENT, UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(patch("/teacher/classrooms/" + UUID.randomUUID() + "/mystery/" + UUID.randomUUID())
                .with(authentication(studentAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void reviewMystery_notFound_returns404() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        when(teacherClassroomService.reviewMysterySubmission(eq(classroomId), eq(submissionId), any(), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId + "/mystery/" + submissionId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void reviewMystery_notOwnedByTeacher_returns403() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        when(teacherClassroomService.reviewMysterySubmission(eq(classroomId), eq(submissionId), any(), any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/teacher/classrooms/" + classroomId + "/mystery/" + submissionId)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
            .andExpect(status().isForbidden());
    }

}
