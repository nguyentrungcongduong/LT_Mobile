package com.gymapp.modules.membership.service;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.branch.entity.Branch;
import com.gymapp.modules.branch.repository.BranchRepository;
import com.gymapp.modules.membership.dto.CreateMembershipPlanRequest;
import com.gymapp.modules.membership.dto.MembershipPlanListResponse;
import com.gymapp.modules.membership.dto.MembershipPlanResponse;
import com.gymapp.modules.membership.entity.MembershipPlan;
import com.gymapp.modules.membership.enums.PlanType;
import com.gymapp.modules.membership.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final BranchRepository branchRepository;

    @Transactional
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        // Step 1: Tạo entity base và map base fields
        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setPlanType(request.getPlanType());
        plan.setActive(true);

        // Step 2: Branching theo plan_type
        if (request.getPlanType() == PlanType.SINGLE) {
            // CASE 1: SINGLE
            if (request.getBranchId() == null) {
                throw new BadRequestException("BRANCH_REQUIRED", "Chi nhánh là bắt buộc cho gói SINGLE");
            }
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BadRequestException("BRANCH_NOT_FOUND", "Chi nhánh không tồn tại"));
            plan.setBranch(branch);
        } else {
            // CASE 2: ALL
            plan.setBranch(null);
        }

        // Step 5: Save DB
        plan = membershipPlanRepository.save(plan);
        
        // Step 6: Return response
        return mapToResponse(plan);
    }

    @Transactional
    public MembershipPlanResponse updatePlan(UUID id, CreateMembershipPlanRequest request) {
        // Step 1: Lấy plan theo id
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Gói tập không tồn tại"));

        // Step 2: Update field nào có trong request (partial update)
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        if (request.getDurationDays() != null) plan.setDurationDays(request.getDurationDays());

        // Step 3: Nếu có thay đổi plan_type hoặc branch_id → chạy lại logic SINGLE / ALL
        if (request.getPlanType() != null || request.getBranchId() != null) {
            PlanType effectiveType = request.getPlanType() != null ? request.getPlanType() : plan.getPlanType();
            UUID effectiveBranchId = request.getBranchId() != null ? request.getBranchId() : (plan.getBranch() != null ? plan.getBranch().getId() : null);

            if (effectiveType == PlanType.SINGLE) {
                if (effectiveBranchId == null) {
                    throw new BadRequestException("BRANCH_REQUIRED", "Chi nhánh là bắt buộc cho gói SINGLE");
                }
                
                // Chỉ query lại branch nếu branchId thay đổi
                if (request.getBranchId() != null) {
                    Branch branch = branchRepository.findById(request.getBranchId())
                            .orElseThrow(() -> new BadRequestException("BRANCH_NOT_FOUND", "Chi nhánh không tồn tại"));
                    plan.setBranch(branch);
                }
                
                if (request.getPlanType() != null) {
                    plan.setPlanType(PlanType.SINGLE);
                }
            } else if (request.getPlanType() == PlanType.ALL) {
                plan.setPlanType(PlanType.ALL);
                plan.setBranch(null);
            }
        }

        // Step 4: Save
        plan = membershipPlanRepository.save(plan);
        return mapToResponse(plan);
    }

    @Transactional(readOnly = true)
    public MembershipPlanListResponse getAllPlans(UUID branchId, PlanType planType) {
        List<MembershipPlan> plans = membershipPlanRepository.findAllWithFilters(branchId, planType);
        
        List<MembershipPlanResponse> responses = plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new MembershipPlanListResponse(responses);
    }

    private MembershipPlanResponse mapToResponse(MembershipPlan plan) {
        return MembershipPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .planType(plan.getPlanType())
                .branchId(plan.getBranch() != null ? plan.getBranch().getId() : null)
                .branchName(plan.getBranch() != null ? plan.getBranch().getName() : null)
                .isActive(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
