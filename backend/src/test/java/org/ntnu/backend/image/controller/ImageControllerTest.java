package org.ntnu.backend.image.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

class ImageControllerTest {

    private MockMvc mockMvc;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = mock(ImageService.class);

        ImageController imageController = new ImageController(imageService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(imageController)
                .build();
    }

    @Test
    void uploadImage_shouldReturnSavedImageMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "detective.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        ImageEntity savedImage = new ImageEntity();
        savedImage.setId(1L);
        savedImage.setObjectKey("images/test-image.png");
        savedImage.setOriginalFilename("detective.png");
        savedImage.setContentType(MediaType.IMAGE_PNG_VALUE);
        savedImage.setSize(file.getSize());
        savedImage.setUploadedAt(LocalDateTime.of(2026, 4, 28, 12, 30));

        when(imageService.uploadImage(any(MultipartFile.class)))
                .thenReturn(savedImage);

        mockMvc.perform(multipart("/images")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.objectKey").value("images/test-image.png"))
                .andExpect(jsonPath("$.originalFilename").value("detective.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.size").value(file.getSize()));

        verify(imageService).uploadImage(any(MultipartFile.class));
    }
}