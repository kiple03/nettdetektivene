package org.ntnu.backend.image.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.dto.TeacherImageResponse;
import org.ntnu.backend.image.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Service for uploading and retrieving images via Amazon S3.
 *
 * <p>Uploaded images are stored in the S3 bucket configured by {@code s3.bucket} under
 * the {@code images/} prefix with a UUID-based key, ensuring no filename collisions.
 * Metadata (original filename, content type, size, upload time) is persisted in the
 * database via {@link ImageRepository} so images can be looked up by ID without
 * querying S3 directly.
 *
 * <p>Only JPEG, PNG, WEBP, and GIF files up to {@value MAX_FILE_SIZE} MB are accepted.
 * The S3 bucket is created automatically on first use if it does not already exist.
 */
@Service
public class ImageService {

    /** MIME types accepted for upload. */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    @Value("${s3.bucket}")
    private String bucketName;

    @Value("${s3.public-base-url}")
    private String publicBaseUrl;

    /**
     * Constructs an {@code ImageService} with the given S3 client and image repository.
     *
     * @param s3Client        the AWS S3 client used for bucket and object operations
     * @param imageRepository the repository used to persist and retrieve image metadata
     */
    public ImageService(S3Client s3Client, ImageRepository imageRepository) {
        this.s3Client = s3Client;
        this.imageRepository = imageRepository;
    }

    /**
     * Validates, uploads, and records an image file.
     *
     * <p>The file is validated for type and size, then stored in S3 under a
     * randomly generated key of the form {@code images/<uuid><extension>}.
     * A corresponding {@link ImageEntity} with metadata is saved to the database
     * and returned.
     *
     * @param file the multipart image file to upload
     * @return the persisted {@link ImageEntity} containing the S3 key and metadata
     * @throws IOException      if reading the file bytes fails
     * @throws RuntimeException if the file is empty, an unsupported type, or exceeds 5 MB
     */
    public TeacherImageResponse uploadImage(MultipartFile file, UUID uploadedByUserId) throws IOException {
        validateImage(file);
        ensureBucketExists();

        String extension = getFileExtension(file.getOriginalFilename());
        String objectKey = UUID.randomUUID() + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        ImageEntity image = new ImageEntity();
        image.setObjectKey(objectKey);
        image.setOriginalFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setUploadedAt(LocalDateTime.now());
        image.setUploadedByUserId(uploadedByUserId);

        ImageEntity saved = imageRepository.save(image);
        return new TeacherImageResponse(saved.getId(), buildPublicUrl(objectKey),
                saved.getOriginalFilename(), saved.getUploadedAt());
    }

    public List<TeacherImageResponse> getTeacherImages(UUID teacherId) {
        return imageRepository.findByUploadedByUserIdOrderByUploadedAtDesc(teacherId).stream()
                .map(img -> new TeacherImageResponse(img.getId(), buildPublicUrl(img.getObjectKey()),
                        img.getOriginalFilename(), img.getUploadedAt()))
                .toList();
    }

    private String buildPublicUrl(String objectKey) {
        return publicBaseUrl + "/" + bucketName + "/" + objectKey;
    }

    /**
     * Validates that the file is non-empty, of an accepted MIME type, and within the size limit.
     *
     * @param file the file to validate
     * @throws RuntimeException if any validation check fails
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Image file is empty");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Only JPEG, PNG, WEBP, and GIF are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Image file is too large");
        }
    }

    /**
     * Ensures the configured S3 bucket exists, creating it if necessary.
     *
     * <p>Bucket existence is checked via a {@code HeadBucket} request. Any exception
     * (typically {@code NoSuchBucketException}) is treated as absence and triggers
     * a {@code CreateBucket} request.
     */
    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    /**
     * Extracts the file extension from a filename, including the leading dot.
     * Falls back to {@code ".jpg"} if the filename is {@code null} or has no extension.
     *
     * @param filename the original filename (it may be {@code null})
     * @return the extension string, e.g. {@code ".png"}, or {@code ".jpg"} as a fallback
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}