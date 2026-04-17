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

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageEntity> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageEntity savedImage = imageService.uploadImage(file);
        return ResponseEntity.ok(savedImage);
    }

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