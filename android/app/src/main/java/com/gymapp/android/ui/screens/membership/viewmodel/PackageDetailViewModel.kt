package com.gymapp.android.ui.screens.membership.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.usecase.membership.GetMembershipPlanByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageDetailUiState(
    val isLoading: Boolean = true,
    val plan: MembershipPlan? = null,
    val error: String? = null
)

@HiltViewModel
class PackageDetailViewModel @Inject constructor(
    private val getMembershipPlanByIdUseCase: GetMembershipPlanByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("planId")?.let { id ->
            loadPlan(id)
        } ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy ID gói tập") }
        }
    }

    private fun loadPlan(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getMembershipPlanByIdUseCase(id)
            if (result.isSuccess) {
                val plan = result.getOrNull()
                if (plan != null) {
                    _uiState.update { it.copy(isLoading = false, plan = plan) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Gói tập không tồn tại") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải dữ liệu") }
            }
        }
    }
}
