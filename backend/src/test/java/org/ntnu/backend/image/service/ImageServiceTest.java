package org.ntnu.backend.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.repository.ImageRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Optional;

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
        ReflectionTestUtils.setField(imageService, "bucketName", "images");
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

        ImageEntity result = imageService.uploadImage(file);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test.png", result.getOriginalFilename());
        assertEquals("image/png", result.getContentType());
        assertEquals(file.getSize(), result.getSize());
        assertNotNull(result.getUploadedAt());
        assertNotNull(result.getObjectKey());
        assertTrue(result.getObjectKey().startsWith("images/"));
        assertTrue(result.getObjectKey().endsWith(".png"));

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

        imageService.uploadImage(file);

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
                () -> imageService.uploadImage(file));

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
                () -> imageService.uploadImage(file));

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
                () -> imageService.uploadImage(file));

        assertEquals("Image file is too large", exception.getMessage());
        verifyNoInteractions(imageRepository);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void getImageMetadata_shouldReturnImageMetadata() {
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setObjectKey("images/test.png");
        image.setOriginalFilename("test.png");
        image.setContentType("image/png");
        image.setSize(123L);
        image.setUploadedAt(LocalDateTime.now());

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        ImageEntity result = imageService.getImageMetadata(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("images/test.png", result.getObjectKey());
    }

    @Test
    void getImageMetadata_shouldThrowWhenImageNotFound() {
        when(imageRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.getImageMetadata(99L));

        assertEquals("Image not found", exception.getMessage());
    }

    @Test
    void getImage_shouldReturnStreamFromS3() {
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setObjectKey("images/test.png");

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        new ByteArrayInputStream("img".getBytes())
                );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        ResponseInputStream<GetObjectResponse> result = imageService.getImage(1L);

        assertNotNull(result);

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());

        GetObjectRequest request = captor.getValue();
        assertEquals("images", request.bucket());
        assertEquals("images/test.png", request.key());
    }

    @Test
    void getImage_shouldThrowWhenImageNotFound() {
        when(imageRepository.findById(42L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.getImage(42L));

        assertEquals("Image not found", exception.getMessage());
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }
}