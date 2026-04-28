package org.ntnu.backend.image.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        when(imageService.uploadImage(any())).thenReturn(savedImage);

        mockMvc.perform(multipart("/images")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.objectKey").value("images/test-image.png"))
                .andExpect(jsonPath("$.originalFilename").value("detective.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.size").value(file.getSize()));

        verify(imageService).uploadImage(any());
    }

    @Test
    void getImage_shouldReturnImageBytesWithCorrectHeaders() throws Exception {
        byte[] imageBytes = "fake-image-bytes".getBytes();

        ImageEntity metadata = new ImageEntity();
        metadata.setId(1L);
        metadata.setObjectKey("images/test-image.png");
        metadata.setOriginalFilename("detective.png");
        metadata.setContentType(MediaType.IMAGE_PNG_VALUE);
        metadata.setSize((long) imageBytes.length);

        GetObjectResponse getObjectResponse = GetObjectResponse.builder()
                .contentType(MediaType.IMAGE_PNG_VALUE)
                .contentLength((long) imageBytes.length)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(
                        getObjectResponse,
                        AbortableInputStream.create(new ByteArrayInputStream(imageBytes))
                );

        when(imageService.getImageMetadata(1L)).thenReturn(metadata);
        when(imageService.getImage(1L)).thenReturn(responseInputStream);

        mockMvc.perform(get("/images/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"detective.png\""
                ))
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageBytes));

        verify(imageService).getImageMetadata(1L);
        verify(imageService).getImage(1L);
    }
}