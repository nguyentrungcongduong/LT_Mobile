package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtListDto {
    private UUID id;
    private String fullName;
    private String avatarUrl;
    private List<String> specializations;
    private BigDecimal pricePerSession;
    private BigDecimal ratingAvg;
    private Integer totalReviews;
    private Integer yearsExperience;
    private boolean isApproved;
}
