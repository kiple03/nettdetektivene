package org.ntnu.backend.image.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.ntnu.backend.image.ImageEntity;
import org.ntnu.backend.image.dto.TeacherImageResponse;
import org.ntnu.backend.image.service.ImageService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ImageEntity> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) throws IOException {
        ImageEntity savedImage = imageService.uploadImage(file, principal.getUserId());
        return ResponseEntity.ok(savedImage);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) throws IOException {
        ImageEntity metadata = imageService.getImageMetadata(id);

        try (ResponseInputStream<GetObjectResponse> imageStream = imageService.getImage(id)) {
            byte[] imageBytes = imageStream.readAllBytes();

            String disposition = ContentDisposition.inline()
                    .filename(metadata.getOriginalFilename(), StandardCharsets.UTF_8)
                    .build().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(imageBytes);
        }
    }

    @GetMapping("/teacher/images")
    @PreAuthorize("hasRole('TEACHER')")
    public List<TeacherImageResponse> listMyImages(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return imageService.getTeacherImages(principal.getUserId());
    }
}