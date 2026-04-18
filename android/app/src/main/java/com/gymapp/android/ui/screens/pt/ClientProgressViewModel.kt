package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.ClientProgressDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientProgressUiState {
    object Loading : ClientProgressUiState()
    data class Success(val progress: ClientProgressDto) : ClientProgressUiState()
    data class Error(val message: String) : ClientProgressUiState()
}

@HiltViewModel
class ClientProgressViewModel @Inject constructor(
    private val ptRepository: PtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: String = savedStateHandle.get<String>("userId") ?: ""
    val clientName: String = savedStateHandle.get<String>("clientName") ?: "Client"
    val totalSessions: Long = savedStateHandle.get<Long>("totalSessions") ?: 0L
    val lastSessionAt: String = savedStateHandle.get<String>("lastSessionAt") ?: "none"

    private val _uiState = MutableStateFlow<ClientProgressUiState>(ClientProgressUiState.Loading)
    val uiState: StateFlow<ClientProgressUiState> = _uiState.asStateFlow()

    init {
        if (userId.isNotEmpty()) {
            loadProgress()
        } else {
            _uiState.value = ClientProgressUiState.Error("ID khách hàng không hợp lệ")
        }
    }

    fun loadProgress() {
        _uiState.value = ClientProgressUiState.Loading
        viewModelScope.launch {
            ptRepository.getClientProgress(userId)
                .onSuccess { progress ->
                    _uiState.value = ClientProgressUiState.Success(progress)
                }
                .onFailure { error ->
                    _uiState.value = ClientProgressUiState.Error(error.message ?: "Lỗi tải tiến độ client")
                }
        }
    }
}
