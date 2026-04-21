package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtDetailDto {
    private UUID id;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private List<String> specializations;
    private BigDecimal pricePerSession;
    private BigDecimal ratingAvg;
    private Integer totalReviews;
    private Integer yearsExperience;
    private List<String> certificateUrls;
    private List<ReviewDto> reviews;
}
