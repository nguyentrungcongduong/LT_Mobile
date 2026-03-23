package com.gymapp.modules.membership.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.membership.dto.CreateMembershipPlanRequest;
import com.gymapp.modules.membership.dto.MembershipPlanResponse;
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
            @RequestBody CreateMembershipPlanRequest request) {
        MembershipPlanResponse data = membershipPlanService.updatePlan(id, request);
        return ApiResponse.ok(data, "Cập nhật gói tập thành công");
    }
}
