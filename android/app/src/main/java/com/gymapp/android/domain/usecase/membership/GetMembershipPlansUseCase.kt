package com.gymapp.android.domain.usecase.membership

import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.repository.MembershipRepository
import javax.inject.Inject

class GetMembershipPlansUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(branchId: String? = null, planType: String? = null): Result<List<MembershipPlan>> {
        return repository.getMembershipPlans(branchId, planType)
    }
}
