package com.gymapp.modules.membership.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.membership.enums.PlanType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMembershipPlanRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private PlanType planType;
    private UUID branchId;

    @JsonProperty("isActive")
    private Boolean isActive;
}
