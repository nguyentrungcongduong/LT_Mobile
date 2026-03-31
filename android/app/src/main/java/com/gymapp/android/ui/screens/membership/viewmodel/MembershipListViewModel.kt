package com.gymapp.android.ui.screens.membership.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.domain.model.membership.PlanType
import com.gymapp.android.domain.usecase.membership.GetMembershipPlansUseCase
import com.gymapp.android.ui.screens.membership.event.MembershipListEvent
import com.gymapp.android.ui.screens.membership.state.MembershipListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembershipListViewModel @Inject constructor(
    private val getMembershipPlansUseCase: GetMembershipPlansUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembershipListUiState())
    val uiState: StateFlow<MembershipListUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun onEvent(event: MembershipListEvent) {
        when (event) {
            is MembershipListEvent.Refresh -> loadPlans()
            is MembershipListEvent.OnFilterChanged -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
                loadPlans(event.filter)
            }
            is MembershipListEvent.OnPackageClicked -> {
                // Handle navigation or payment prompt later
            }
        }
    }

    private fun loadPlans(filter: String = _uiState.value.selectedFilter) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val planType = when (filter) {
                "1 Chi nhánh" -> PlanType.SINGLE.name
                "Toàn chuỗi" -> PlanType.ALL.name
                else -> null
            }
            val result = getMembershipPlansUseCase.invoke(planType = planType)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        plans = result.getOrNull() ?: emptyList()
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
                    )
                }
            }
        }
    }
}
