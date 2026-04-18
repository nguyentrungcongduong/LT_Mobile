package com.gymapp.modules.membership.service.impl;

import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.membership.dto.response.ActiveMembershipResponse;
import com.gymapp.modules.membership.entity.Membership;
import com.gymapp.modules.membership.entity.MembershipPlan;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.repository.MembershipPlanRepository;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.membership.service.UserMembershipService;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserMembershipServiceImpl implements UserMembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;

    @Override
    public ActiveMembershipResponse getActiveMembershipForCurrentUser() {
        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        
        // Find latest membership first
        Optional<Membership> latestMembershipOpt = membershipRepository.findLatestMembershipsByUserId(userId).stream().findFirst();
        
        if (latestMembershipOpt.isEmpty()) {
            throw new ResourceNotFoundException("NO_MEMBERSHIP", "No membership found for current user");
        }
        
        Membership membership = latestMembershipOpt.get();
        // Calculate days left
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());
        if (daysLeft < 0) daysLeft = 0;
        
        return ActiveMembershipResponse.builder()
                .id(membership.getId())
                .planName(membership.getPlan().getName())
                .planType(membership.getPlan().getPlanType())
                .branchName(membership.getBranch() != null ? membership.getBranch().getName() : null)
                .status(membership.getStatus())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .daysLeft(daysLeft)
                .build();
    }

    @Override
    public ActiveMembershipResponse registerMembership(UUID planId) {
        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
                
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Plan not found"));
                
        // Create new membership in PENDING status
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());
        
        Membership membership = Membership.builder()
                .user(userRepository.findById(userId).orElseThrow())
                .plan(plan)
                .branch(plan.getBranch())
                .status(MembershipStatus.PENDING)
                .startDate(startDate)
                .endDate(endDate)
                .build();
                
        membershipRepository.save(membership);
        
        return ActiveMembershipResponse.builder()
                .id(membership.getId())
                .planName(plan.getName())
                .planType(plan.getPlanType())
                .branchName(plan.getBranch() != null ? plan.getBranch().getName() : null)
                .status(membership.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .daysLeft(plan.getDurationDays())
                .build();
    }
}
