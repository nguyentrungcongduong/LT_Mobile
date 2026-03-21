package com.gymapp.modules.membership.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.membership.dto.MembershipPlanListResponse;
import com.gymapp.modules.membership.enums.PlanType;
import com.gymapp.modules.membership.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membership-plans")
@RequiredArgsConstructor
public class PublicMembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @GetMapping
    public ApiResponse<MembershipPlanListResponse> getAllPlans(
            @RequestParam(name = "branch_id", required = false) UUID branch_id,
            @RequestParam(name = "plan_type", required = false) PlanType plan_type) {
        MembershipPlanListResponse data = membershipPlanService.getAllPlans(branch_id, plan_type);
        return ApiResponse.ok(data);
    }
}
