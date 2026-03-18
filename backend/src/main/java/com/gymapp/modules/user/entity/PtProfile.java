package com.gymapp.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pt_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PtProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Postgres Array mapping might require hypersistence, but for now we can map as List using specific types 
    // or standard element collection if needed. To map exactly to TEXT[] in postgres, we need Hibernate 6 features
    // or @JdbcTypeCode(SqlTypes.ARRAY).
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(name = "specializations", columnDefinition = "text[]")
    private List<String> specializations;

    @Column(name = "price_per_session", nullable = false)
    @Builder.Default
    private BigDecimal pricePerSession = BigDecimal.ZERO;

    @Column(name = "rating_avg", nullable = false)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.valueOf(0.00);

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(name = "certificate_urls", columnDefinition = "text[]")
    private List<String> certificateUrls;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean isApproved = false;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
