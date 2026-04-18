package com.gymapp.modules.membership.repository;

import com.gymapp.modules.membership.entity.MembershipPlan;
import com.gymapp.modules.membership.enums.PlanType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {

    @Query("SELECT p FROM MembershipPlan p " +
            "WHERE (cast(:branchId as text) IS NULL OR p.branch.id = :branchId) " +
            "AND (cast(:planType as text) IS NULL OR p.planType = :planType) " +
            "AND p.isActive = true")
    List<MembershipPlan> findAllWithFilters(
            @Param("branchId") UUID branchId,
            @Param("planType") PlanType planType);
}
