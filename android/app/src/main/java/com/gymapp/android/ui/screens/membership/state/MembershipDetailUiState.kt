package com.gymapp.android.ui.screens.membership.state

import com.gymapp.android.domain.model.membership.ActiveMembership

sealed class MembershipDetailUiState {
    object Loading : MembershipDetailUiState()
    data class Success(val activeMembership: ActiveMembership) : MembershipDetailUiState()
    object Empty : MembershipDetailUiState()
    data class Error(val message: String) : MembershipDetailUiState()
}
