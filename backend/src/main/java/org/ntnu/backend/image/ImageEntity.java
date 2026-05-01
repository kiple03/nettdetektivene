package org.ntnu.backend.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing an uploaded image stored in object storage (S3).
 */
@Entity
@Table(name = "images")
@Data
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** S3 object key; excluded from JSON serialization. */
    @JsonIgnore
    @Column(nullable=false)
    private String objectKey;

    /** Original filename as provided by the uploader. */
    @Column(nullable = false)
    private String originalFilename;

    /** MIME type of the image (e.g. {@code image/png}). */
    @Column(nullable = false)
    private String contentType;

    /** File size in bytes. */
    @Column(nullable = false)
    private Long size;

    /** Timestamp when the image was uploaded. */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /** ID of the user who uploaded the image, or {@code null} if anonymous. */
    @Column(nullable = true)
    private UUID uploadedByUserId;

}
