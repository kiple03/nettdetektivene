package org.ntnu.backend.image.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.image.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence layer for {@link ImageEntity} records. */
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    /**
     * Returns all images uploaded by a specific user, newest first.
     *
     * @param uploadedByUserId the uploader's UUID
     * @return ordered list of images, may be empty
     */
    List<ImageEntity> findByUploadedByUserIdOrderByUploadedAtDesc(UUID uploadedByUserId);
}