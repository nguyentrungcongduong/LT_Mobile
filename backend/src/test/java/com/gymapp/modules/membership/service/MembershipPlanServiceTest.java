package com.gymapp.modules.membership.service;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.modules.branch.repository.BranchRepository;
import com.gymapp.modules.membership.dto.CreateMembershipPlanRequest;
import com.gymapp.modules.membership.enums.PlanType;
import com.gymapp.modules.membership.repository.MembershipPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipPlanServiceTest {

    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private MembershipPlanService membershipPlanService;

    private CreateMembershipPlanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CreateMembershipPlanRequest.builder()
                .name("Gói Tháng")
                .price(new BigDecimal("500000"))
                .durationDays(30)
                .planType(PlanType.ALL)
                .build();
    }

    @Test
    void createPlan_WhenSingleTypeAndBranchIdMissing_ShouldThrowBadRequest() {
        validRequest.setPlanType(PlanType.SINGLE);
        validRequest.setBranchId(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            membershipPlanService.createPlan(validRequest);
        });

        assertEquals("BRANCH_REQUIRED", ex.getErrorCode());
    }

    @Test
    void createPlan_WhenBranchNotFound_ShouldThrowBadRequest() {
        UUID branchId = UUID.randomUUID();
        validRequest.setPlanType(PlanType.SINGLE);
        validRequest.setBranchId(branchId);

        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            membershipPlanService.createPlan(validRequest);
        });

        assertEquals("BRANCH_NOT_FOUND", ex.getErrorCode());
    }
}
