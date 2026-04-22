package com.gymapp.modules.membership.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

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
    private Integer durationDays;
    private PlanType planType;
    private UUID branchId;
    private String branchName;
    private BigDecimal branchLatitude;
    private BigDecimal branchLongitude;
    /** false khi SINGLE plan gắn với branch đang tạm ngưng */
    private boolean branchIsActive;

    // Primitive boolean → Lombok generates isActive() getter → Jackson serializes as "active"
    // Frontend type uses `active: boolean` to match this behavior
    private boolean isActive;

    private java.util.List<com.gymapp.modules.branch.dto.BranchResponse> availableBranches;

    private OffsetDateTime createdAt;
}
