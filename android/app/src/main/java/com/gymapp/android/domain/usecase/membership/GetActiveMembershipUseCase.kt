package com.gymapp.android.domain.usecase.membership

import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.repository.MembershipRepository
import javax.inject.Inject

class GetActiveMembershipUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(): Result<ActiveMembership> {
        return repository.getActiveMembership()
    }
}
