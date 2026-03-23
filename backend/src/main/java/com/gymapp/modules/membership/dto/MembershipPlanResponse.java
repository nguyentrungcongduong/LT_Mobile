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
    private boolean isActive;
    private OffsetDateTime createdAt;
}
