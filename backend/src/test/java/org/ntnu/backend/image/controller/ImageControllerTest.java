package org.ntnu.backend.image.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.config.SecurityConfig;
import org.ntnu.backend.image.dto.TeacherImageResponse;
import org.ntnu.backend.image.service.ImageService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationEntryPoint;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

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

    // GET /teacher/images

    @Test
    void listMyImages_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        TeacherImageResponse img = new TeacherImageResponse(1L, "http://s3.example.com/bucket/uuid.png", "photo.png", now);
        when(imageService.getTeacherImages(eq(teacherId))).thenReturn(List.of(img));

        mockMvc.perform(get("/teacher/images")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].url").value("http://s3.example.com/bucket/uuid.png"))
            .andExpect(jsonPath("$[0].originalFilename").value("photo.png"));
    }

    @Test
    void listMyImages_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/teacher/images"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listMyImages_studentRole_returns403() throws Exception {
        mockMvc.perform(get("/teacher/images")
                .with(authentication(studentAuth())))
            .andExpect(status().isForbidden());
    }

    @Test
    void listMyImages_emptyGallery_returnsEmptyArray() throws Exception {
        UUID teacherId = UUID.randomUUID();
        when(imageService.getTeacherImages(eq(teacherId))).thenReturn(List.of());

        mockMvc.perform(get("/teacher/images")
                .with(authentication(teacherAuth(teacherId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // POST /images

    @Test
    void uploadImage_noAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        mockMvc.perform(multipart("/images").file(file))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadImage_studentRole_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        mockMvc.perform(multipart("/images").file(file)
                .with(authentication(studentAuth()))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isForbidden());
    }

    @Test
    void uploadImage_withAuth_returnsOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        TeacherImageResponse response = new TeacherImageResponse(
                5L, "http://s3.example.com/bucket/uuid.png", "test.png", LocalDateTime.now());

        when(imageService.uploadImage(any(), eq(teacherId))).thenReturn(response);

        mockMvc.perform(multipart("/images").file(file)
                .with(authentication(teacherAuth(teacherId)))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.url").value("http://s3.example.com/bucket/uuid.png"));
    }
}
