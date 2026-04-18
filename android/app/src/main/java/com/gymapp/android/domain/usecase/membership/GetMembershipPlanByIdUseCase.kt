package com.gymapp.android.domain.usecase.membership

import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.repository.MembershipRepository
import javax.inject.Inject

class GetMembershipPlanByIdUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(id: String): Result<MembershipPlan?> {
        return repository.getMembershipPlanById(id)
    }
}
