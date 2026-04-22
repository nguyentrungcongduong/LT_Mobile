package com.gymapp.modules.membership.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.membership.dto.CreateMembershipPlanRequest;
import com.gymapp.modules.membership.dto.MembershipPlanListResponse;
import com.gymapp.modules.membership.dto.MembershipPlanResponse;
import com.gymapp.modules.membership.dto.UpdateMembershipPlanRequest;
import com.gymapp.modules.membership.enums.PlanType;
import com.gymapp.modules.membership.service.MembershipPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/membership-plans")
@RequiredArgsConstructor
public class AdminMembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    /** Admin: xem TẤT CẢ plan (kể cả inactive) để quản lý */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MembershipPlanListResponse> getAllPlans(
            @RequestParam(name = "branch_id", required = false) UUID branchId,
            @RequestParam(name = "plan_type", required = false) PlanType planType) {
        MembershipPlanListResponse data = membershipPlanService.getAllPlans(branchId, planType);
        return ApiResponse.ok(data);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MembershipPlanResponse> createPlan(@Valid @RequestBody CreateMembershipPlanRequest request) {
        MembershipPlanResponse data = membershipPlanService.createPlan(request);
        return ApiResponse.ok(data, "Tạo gói tập thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MembershipPlanResponse> updatePlan(
            @PathVariable(name = "id") UUID id,
            @RequestBody UpdateMembershipPlanRequest request) {
        MembershipPlanResponse data = membershipPlanService.updatePlan(id, request);
        return ApiResponse.ok(data, "Cập nhật gói tập thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePlan(@PathVariable(name = "id") UUID id) {
        membershipPlanService.deletePlan(id);
        return ApiResponse.ok(null, "Xóa gói tập thành công");
    }
}
