package com.gymapp.android.domain.repository

import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.MembershipPlan

interface MembershipRepository {

    suspend fun getMembershipPlans(
        branchId: String?,
        planType: String?
    ): Result<List<MembershipPlan>>

    suspend fun getMembershipPlanById(id: String): Result<MembershipPlan?>

    suspend fun getActiveMembership(): Result<ActiveMembership>

    suspend fun registerMembership(planId: String): Result<ActiveMembership>
}
