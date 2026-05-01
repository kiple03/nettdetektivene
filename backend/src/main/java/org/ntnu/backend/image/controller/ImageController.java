package org.ntnu.backend.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
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

/** REST controller for teacher image upload and retrieval. */
@RestController
@AllArgsConstructor
@Tag(name = "Images", description = "Image upload and retrieval for teachers")
public class ImageController {

    private final ImageService imageService;

    /**
     * Uploads an image file for the authenticated teacher.
     *
     * @param file the multipart image file to upload
     * @param principal the authenticated teacher
     * @return the uploaded image metadata
     * @throws IOException if the file cannot be read
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Upload an image",
        description = "Uploads a multipart image file and stores it. Returns metadata including the image URL"
    )
    public ResponseEntity<TeacherImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(file, principal.getUserId()));
    }

    /**
     * Returns all images uploaded by the authenticated teacher.
     *
     * @param principal the authenticated teacher
     * @return list of image metadata belonging to the teacher
     */
    @GetMapping("/teacher/images")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "List all images uploaded by the authenticated teacher")
    public List<TeacherImageResponse> listMyImages(
            @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return imageService.getTeacherImages(principal.getUserId());
    }
}
