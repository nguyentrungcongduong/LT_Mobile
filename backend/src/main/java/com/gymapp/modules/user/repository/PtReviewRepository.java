package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.PtReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PtReviewRepository extends JpaRepository<PtReview, UUID> {

    List<PtReview> findByPtIdOrderByCreatedAtDesc(UUID ptId);

    boolean existsByUserIdAndPtId(UUID userId, UUID ptId);

    Optional<PtReview> findByUserIdAndPtId(UUID userId, UUID ptId);

    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0) FROM PtReview r WHERE r.pt.id = :ptId")
    Double calculateAvgRating(@Param("ptId") UUID ptId);

    long countByPtId(UUID ptId);
}
