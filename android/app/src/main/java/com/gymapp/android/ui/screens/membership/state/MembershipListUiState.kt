package com.gymapp.android.ui.screens.membership.state

import com.gymapp.android.domain.model.membership.MembershipPlan

data class MembershipListUiState(
    val isLoading: Boolean = false,
    val plans: List<MembershipPlan> = emptyList(),
    val error: String? = null,
    val selectedFilter: String = "Tất cả"
)
