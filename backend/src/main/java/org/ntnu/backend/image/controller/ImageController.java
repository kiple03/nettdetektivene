package org.ntnu.backend.image.controller;

import java.io.IOException;
import java.util.List;
import org.ntnu.backend.image.dto.TeacherImageResponse;
import org.ntnu.backend.image.service.ImageService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(file, principal.getUserId()));
    }

    @GetMapping("/teacher/images")
    @PreAuthorize("hasRole('TEACHER')")
    public List<TeacherImageResponse> listMyImages(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return imageService.getTeacherImages(principal.getUserId());
    }
}
