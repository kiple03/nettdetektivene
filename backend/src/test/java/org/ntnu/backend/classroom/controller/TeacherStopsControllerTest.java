package org.ntnu.backend.classroom.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.service.TeacherClassroomService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeacherStopsController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class TeacherStopsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherClassroomService teacherClassroomService;

    @MockitoBean
    private JwtService jwtService;

    private JwtAuthenticationToken teacherAuth(UUID userId) {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "teacher1", userId, "TEACHER", null,
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    @Test
    void listStops_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();

        StopResponse stop = StopResponse.builder()
            .id(1)
            .name("City Hall")
            .displayOrder(1)
            .build();

        when(teacherClassroomService.listStops()).thenReturn(List.of(stop));

        mockMvc.perform(get("/teacher/stops")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("City Hall"))
            .andExpect(jsonPath("$[0].displayOrder").value(1));
    }

    @Test
    void listStops_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/stops"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listStops_studentRole_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
            "student1", userId, "STUDENT", UUID.randomUUID(),
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        JwtAuthenticationToken studentAuth = new JwtAuthenticationToken(principal, principal.getAuthorities());

        mockMvc.perform(get("/teacher/stops")
                .with(authentication(studentAuth)))
            .andExpect(status().isForbidden());
    }
}
