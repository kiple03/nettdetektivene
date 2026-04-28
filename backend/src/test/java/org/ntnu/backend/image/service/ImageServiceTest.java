package org.ntnu.backend.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.dto.TeacherImageResponse;
import org.ntnu.backend.image.repository.ImageRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(imageService, "publicBaseUrl", "http://s3.example.com");
    }

    @Test
    void uploadImage_shouldUploadToS3AndSaveMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(invocation -> {
            ImageEntity image = invocation.getArgument(0);
            image.setId(1L);
            return image;
        });

        UUID uploaderId = UUID.randomUUID();
        TeacherImageResponse result = imageService.uploadImage(file, uploaderId);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("test.png", result.originalFilename());
        assertNotNull(result.uploadedAt());
        assertTrue(result.url().startsWith("http://s3.example.com/test-bucket/"));
        assertTrue(result.url().endsWith(".png"));

        verify(s3Client).headBucket(any(HeadBucketRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
        verify(imageRepository).save(any(ImageEntity.class));
    }

    @Test
    void uploadImage_shouldCreateBucketIfMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        doThrow(NoSuchBucketException.builder().message("Bucket not found").build())
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        imageService.uploadImage(file, UUID.randomUUID());

        verify(s3Client).headBucket(any(HeadBucketRequest.class));
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
        verify(imageRepository).save(any(ImageEntity.class));
    }

    @Test
    void uploadImage_shouldThrowWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.uploadImage(file, UUID.randomUUID()));

        assertEquals("Image file is empty", exception.getMessage());
        verifyNoInteractions(imageRepository);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void uploadImage_shouldThrowWhenFileTypeIsInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                "fake-content".getBytes()
        );

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.uploadImage(file, UUID.randomUUID()));

        assertEquals("Only JPEG, PNG, and WEBP are allowed", exception.getMessage());
        verifyNoInteractions(imageRepository);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void uploadImage_shouldThrowWhenFileIsTooLarge() {
        byte[] largeContent = new byte[5 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                largeContent
        );

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.uploadImage(file, UUID.randomUUID()));

        assertEquals("Image file is too large", exception.getMessage());
        verifyNoInteractions(imageRepository);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void getTeacherImages_returnsImagesNewestFirst() {
        UUID teacherId = UUID.randomUUID();

        ImageEntity img1 = new ImageEntity();
        img1.setId(1L);
        img1.setOriginalFilename("a.png");
        img1.setObjectKey("uuid-a.png");
        img1.setUploadedAt(LocalDateTime.now().minusDays(1));
        img1.setUploadedByUserId(teacherId);

        ImageEntity img2 = new ImageEntity();
        img2.setId(2L);
        img2.setOriginalFilename("b.png");
        img2.setObjectKey("uuid-b.png");
        img2.setUploadedAt(LocalDateTime.now());
        img2.setUploadedByUserId(teacherId);

        when(imageRepository.findByUploadedByUserIdOrderByUploadedAtDesc(teacherId))
                .thenReturn(List.of(img2, img1));

        var result = imageService.getTeacherImages(teacherId);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals("http://s3.example.com/test-bucket/uuid-b.png", result.get(0).url());
        assertEquals(1L, result.get(1).id());
        assertEquals("http://s3.example.com/test-bucket/uuid-a.png", result.get(1).url());
    }
}
