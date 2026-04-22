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

        /** Admin: all plans (any status) with optional filters */
        @Query("SELECT p FROM MembershipPlan p " +
                        "WHERE (cast(:branchId as text) IS NULL OR p.branch.id = :branchId) " +
                        "AND (cast(:planType as text) IS NULL OR p.planType = :planType)")
        List<MembershipPlan> findAllWithFilters(
                        @Param("branchId") UUID branchId,
                        @Param("planType") PlanType planType);

        /**
         * Mobile / public: tất cả plan active — filtering được xử lý trong service (Java).
         * Dùng LEFT JOIN để plan loại ALL (không có branch) không bị exclude.
         */
        @Query("SELECT p FROM MembershipPlan p LEFT JOIN FETCH p.branch b " +
                        "WHERE p.isActive = true")
        List<MembershipPlan> findAllActive();
}
