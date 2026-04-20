package com.gymapp.modules.membership.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.membership.enums.PlanType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;

    @JsonProperty("duration_days")
    private Integer durationDays;

    @JsonProperty("plan_type")
    private PlanType planType;

    @JsonProperty("branch_id")
    private UUID branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("branch_latitude")
    private BigDecimal branchLatitude;

    @JsonProperty("branch_longitude")
    private BigDecimal branchLongitude;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("available_branches")
    private java.util.List<com.gymapp.modules.branch.dto.BranchResponse> availableBranches;

    private OffsetDateTime createdAt;
}
