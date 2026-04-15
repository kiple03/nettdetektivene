package org.ntnu.backend.mystery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.enrollment.model.Enrollment;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;
import org.ntnu.backend.user.model.User;

@Entity
@Table(name = "mystery_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MysterySubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MysteryStatusEnum status = MysteryStatusEnum.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_teacher_id")
    private User reviewedByTeacher;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "is_featured", nullable = false)
    private boolean featured;
}
