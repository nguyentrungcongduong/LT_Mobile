package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtProfileDto {
    private UUID id;
    private UUID userId;
    private String bio;
    private List<String> specializations;
    private BigDecimal pricePerSession;
    private BigDecimal ratingAvg;
    private Integer totalReviews;
    private Integer yearsExperience;
    private List<String> certificateUrls;
    private String cvUrl;
    private boolean isApproved;
    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
