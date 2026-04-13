package com.gymapp.android.ui.screens.admin.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.PtPublicDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminPtApprovalUiState {
    object Initial : AdminPtApprovalUiState()
    object Loading : AdminPtApprovalUiState()
    data class Success(val pts: List<PtPublicDto>) : AdminPtApprovalUiState()
    data class Error(val message: String) : AdminPtApprovalUiState()
}

@HiltViewModel
class AdminPtApprovalViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminPtApprovalUiState>(AdminPtApprovalUiState.Initial)
    val uiState: StateFlow<AdminPtApprovalUiState> = _uiState

    fun loadPts() {
        _uiState.value = AdminPtApprovalUiState.Loading
        viewModelScope.launch {
            val result = ptRepository.getPtList(page = 0, size = 100) // Load large page for admin
            result.onSuccess { pageResponse ->
                _uiState.value = AdminPtApprovalUiState.Success(pageResponse.content)
            }.onFailure { e ->
                _uiState.value = AdminPtApprovalUiState.Error(e.message ?: "Không thể kết nối đến máy chủ")
            }
        }
    }

    fun approvePt(ptId: String) {
        viewModelScope.launch {
            val result = ptRepository.approvePt(ptId)
            result.onSuccess {
                loadPts() // Reload list after approval
            }.onFailure { e ->
                // Wait, should we notify? Flow can emit side effect. Just reloading is fine or emit error
                _uiState.value = AdminPtApprovalUiState.Error(e.message ?: "Lỗi khi duyệt PT")
            }
        }
    }

    fun suspendPt(ptId: String) {
        viewModelScope.launch {
            val result = ptRepository.suspendPt(ptId, "Admin đình chỉ")
            result.onSuccess {
                loadPts() // Reload list
            }.onFailure { e ->
                _uiState.value = AdminPtApprovalUiState.Error(e.message ?: "Lỗi khi đình chỉ PT")
            }
        }
    }
}
