package org.ntnu.backend.image.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.image.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByUploadedByUserIdOrderByUploadedAtDesc(UUID uploadedByUserId);
}