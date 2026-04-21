package org.ntnu.backend.image.controller;

import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

/**
 * REST controller exposing image upload and retrieval endpoints under {@code /images}.
 *
 * <p>Delegates storage and retrieval to {@link ImageService}. Images are served inline
 * with the original filename and content type preserved in the response headers.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    /**
     * Constructs an {@code ImageController} with the given image service.
     *
     * @param imageService the service handling S3 upload and retrieval logic
     */
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Uploads a multipart image file and returns its persisted metadata.
     *
     * <p>Accepts JPEG, PNG, and WEBP files up to 5 MB. The file is stored in S3 and
     * its metadata is saved to the database before being returned in the response body.
     *
     * @param file the multipart image file to upload
     * @return {@code 200 OK} with the saved {@link ImageEntity} as the response body
     * @throws IOException      if reading the file bytes fails
     * @throws RuntimeException if the file fails validation (empty, wrong type, or too large)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageEntity> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageEntity savedImage = imageService.uploadImage(file);
        return ResponseEntity.ok(savedImage);
    }

    /**
     * Retrieves and streams the image with the given ID directly to the client.
     *
     * <p>The response sets {@code Content-Disposition: inline} with the original filename
     * so browsers render the image rather than downloading it. The {@code Content-Type}
     * is set to the MIME type recorded at upload time.
     *
     * @param id the database ID of the image to retrieve
     * @return {@code 200 OK} with the raw image bytes and appropriate content headers
     * @throws IOException      if reading from the S3 stream fails
     * @throws RuntimeException if no image with the given ID exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) throws IOException {
        ImageEntity metadata = imageService.getImageMetadata(id);

        try (ResponseInputStream<GetObjectResponse> imageStream = imageService.getImage(id)) {
            byte[] imageBytes = imageStream.readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(imageBytes);
        }
    }
}