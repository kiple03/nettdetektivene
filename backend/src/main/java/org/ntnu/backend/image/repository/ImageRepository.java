package org.ntnu.backend.image.repository;

import org.ntnu.backend.image.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
}