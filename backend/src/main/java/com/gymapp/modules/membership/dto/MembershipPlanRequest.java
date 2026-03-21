package com.gymapp.modules.membership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

import com.gymapp.modules.membership.enums.PlanType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanRequest {

    @NotBlank(message = "NAME_REQUIRED")
    private String name;

    private String description;

    @NotNull(message = "PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_MIN_0")
    private BigDecimal price;

    @NotNull(message = "DURATION_DAYS_REQUIRED")
    @Min(value = 1, message = "DURATION_DAYS_MIN_1")
    private Integer durationDays;

    @NotNull(message = "PLAN_TYPE_REQUIRED")
    private PlanType planType;

    private UUID branchId;
}
