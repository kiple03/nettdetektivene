package org.ntnu.backend.teacher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.teacher.dto.ClassroomSummary;
import org.ntnu.backend.teacher.dto.MysterySubmissionResponse;
import org.ntnu.backend.teacher.dto.NotificationResponse;
import org.ntnu.backend.teacher.dto.StudentDetail;
import org.ntnu.backend.teacher.dto.StudentSummary;
import org.ntnu.backend.teacher.dto.StopSummary;
import org.ntnu.backend.teacher.service.TeacherService;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private TeacherController controller;

    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID SUBMISSION_ID = UUID.randomUUID();

    private JwtAuthenticatedPrincipal principal;

    @BeforeEach
    void setup() {
        principal = new JwtAuthenticatedPrincipal(
                "teacher1",
                TEACHER_ID,
                "ROLE_TEACHER",
                List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        var auth = new JwtAuthenticationToken(principal, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── 1. GET /api/teacher/classrooms ─────────────────────────────────────────

    @Test
    void listClassrooms_returns200WithList() throws Exception {
        var summary = new ClassroomSummary(CLASSROOM_ID.toString(), "Math", null, "ABC123", "2025-01-01T00:00:00", 5);
        when(teacherService.listClassrooms(TEACHER_ID)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/teacher/classrooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CLASSROOM_ID.toString()))
                .andExpect(jsonPath("$[0].title").value("Math"))
                .andExpect(jsonPath("$[0].studentCount").value(5));
    }

    // ── 2. POST /api/teacher/classrooms ────────────────────────────────────────

    @Test
    void createClassroom_returns201() throws Exception {
        var summary = new ClassroomSummary(CLASSROOM_ID.toString(), "Science", null, "XYZ789", "2025-01-01T00:00:00", 0);
        when(teacherService.createClassroom(eq(TEACHER_ID), eq("Science"), any())).thenReturn(summary);

        mockMvc.perform(post("/api/teacher/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Science\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Science"));
    }

    // ── 3. GET /api/teacher/classrooms/{id} ────────────────────────────────────

    @Test
    void getClassroom_returns200() throws Exception {
        var summary = new ClassroomSummary(CLASSROOM_ID.toString(), "History", null, "HIS001", "2025-01-01T00:00:00", 3);
        when(teacherService.getClassroom(TEACHER_ID, CLASSROOM_ID)).thenReturn(summary);

        mockMvc.perform(get("/api/teacher/classrooms/" + CLASSROOM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("History"));
    }

    // ── 4. PATCH /api/teacher/classrooms/{id} ──────────────────────────────────

    @Test
    void updateClassroom_returns200() throws Exception {
        var summary = new ClassroomSummary(CLASSROOM_ID.toString(), "Updated", null, "HIS001", "2025-01-01T00:00:00", 3);
        when(teacherService.updateClassroom(eq(TEACHER_ID), eq(CLASSROOM_ID), eq("Updated"), any())).thenReturn(summary);

        mockMvc.perform(patch("/api/teacher/classrooms/" + CLASSROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    // ── 5. GET /api/teacher/classrooms/{id}/students ───────────────────────────

    @Test
    void listStudents_returns200() throws Exception {
        var student = new StudentSummary(STUDENT_ID.toString(), "alice", 2, 4);
        when(teacherService.listStudents(TEACHER_ID, CLASSROOM_ID)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/teacher/classrooms/" + CLASSROOM_ID + "/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].medalsEarned").value(2));
    }

    // ── 6. GET /api/teacher/classrooms/{id}/students/{studentId} ──────────────

    @Test
    void getStudentDetail_returns200() throws Exception {
        var detail = new StudentDetail(STUDENT_ID.toString(), "alice", List.of(), List.of(), List.of());
        when(teacherService.getStudentDetail(TEACHER_ID, CLASSROOM_ID, STUDENT_ID)).thenReturn(detail);

        mockMvc.perform(get("/api/teacher/classrooms/" + CLASSROOM_ID + "/students/" + STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // ── 7. GET /api/teacher/classrooms/{id}/mystery ────────────────────────────

    @Test
    void listMystery_returns200() throws Exception {
        var submission = new MysterySubmissionResponse(
                SUBMISSION_ID.toString(), "alice", "content", null, "PENDING", false, "2025-01-01T00:00:00Z", null);
        when(teacherService.listMysterySubmissions(TEACHER_ID, CLASSROOM_ID)).thenReturn(List.of(submission));

        mockMvc.perform(get("/api/teacher/classrooms/" + CLASSROOM_ID + "/mystery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentUsername").value("alice"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // ── 8. PATCH /api/teacher/classrooms/{id}/mystery/{submissionId} ───────────

    @Test
    void patchMystery_returns200() throws Exception {
        var updated = new MysterySubmissionResponse(
                SUBMISSION_ID.toString(), "alice", "content", null, "APPROVED", false, "2025-01-01T00:00:00Z", "2025-02-01T00:00:00Z");
        when(teacherService.patchMysterySubmission(eq(TEACHER_ID), eq(CLASSROOM_ID), eq(SUBMISSION_ID), any()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/teacher/classrooms/" + CLASSROOM_ID + "/mystery/" + SUBMISSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // ── 9. GET /api/teacher/notifications ─────────────────────────────────────

    @Test
    void getNotifications_returns200() throws Exception {
        var notif = new NotificationResponse(
                "LEVEL_COMPLETION", "alice completed a level", CLASSROOM_ID.toString(), "alice", "2025-01-01T00:00:00Z");
        when(teacherService.getNotifications(TEACHER_ID)).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/teacher/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("LEVEL_COMPLETION"))
                .andExpect(jsonPath("$[0].studentUsername").value("alice"));
    }

    // ── 10. GET /api/teacher/classrooms/{id}/stops ────────────────────────────

    @Test
    void listStops_returns200() throws Exception {
        var stop = new StopSummary(UUID.randomUUID().toString(), "Stop 1", "desc", 1);
        when(teacherService.listStops(TEACHER_ID, CLASSROOM_ID)).thenReturn(List.of(stop));

        mockMvc.perform(get("/api/teacher/classrooms/" + CLASSROOM_ID + "/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Stop 1"))
                .andExpect(jsonPath("$[0].displayOrder").value(1));
    }
}
