package org.ntnu.backend.image.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.image.service.ImageService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;

class ImageControllerTest {

    private MockMvc mockMvc;
    private ImageService imageService;
    private JwtAuthenticatedPrincipal principal;
    private UUID userId;

    @BeforeEach
    void setUp() {
        imageService = mock(ImageService.class);
        principal = mock(JwtAuthenticatedPrincipal.class);
        userId = UUID.randomUUID();

        org.mockito.Mockito.when(principal.getUserId()).thenReturn(userId);

        ImageController imageController = new ImageController(imageService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(imageController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .build();
    }

    @Test
    void uploadImage_shouldDelegateToServiceAndReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "detective.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/images")
                        .file(file))
                .andExpect(status().isOk());

        verify(imageService).uploadImage(any(MultipartFile.class), eq(userId));
    }

    private HandlerMethodArgumentResolver authenticationPrincipalResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(JwtAuthenticatedPrincipal.class);
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
        };
    }
}