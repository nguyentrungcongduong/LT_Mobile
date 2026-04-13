package com.gymapp.android.ui.screens.membership.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.domain.usecase.membership.GetActiveMembershipUseCase
import com.gymapp.android.ui.screens.membership.event.MembershipDetailEvent
import com.gymapp.android.ui.screens.membership.state.MembershipDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembershipDetailViewModel @Inject constructor(
    private val getActiveMembershipUseCase: GetActiveMembershipUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MembershipDetailUiState>(MembershipDetailUiState.Loading)
    val uiState: StateFlow<MembershipDetailUiState> = _uiState.asStateFlow()

    init {
        loadActiveMembership()
    }

    fun onEvent(event: MembershipDetailEvent) {
        when (event) {
            is MembershipDetailEvent.Refresh -> loadActiveMembership()
            is MembershipDetailEvent.OnShowQrClicked -> {
                // To be implemented: Show QR logic
            }
            is MembershipDetailEvent.OnRenewClicked -> {
                // To be implemented: Navigate to packages
            }
        }
    }

    private fun loadActiveMembership() {
        _uiState.update { MembershipDetailUiState.Loading }
        viewModelScope.launch {
            val result = getActiveMembershipUseCase.invoke()
            if (result.isSuccess) {
                val membership = result.getOrNull()
                if (membership != null) {
                    _uiState.update { MembershipDetailUiState.Success(membership) }
                } else {
                    _uiState.update { MembershipDetailUiState.Empty }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
                if (errorMsg.contains("NO_MEMBERSHIP", ignoreCase = true) || errorMsg.contains("No membership found", ignoreCase = true) || errorMsg.contains("trống")) {
                    _uiState.update { MembershipDetailUiState.Empty }
                } else {
                    _uiState.update { MembershipDetailUiState.Error(errorMsg) }
                }
            }
        }
    }
}
