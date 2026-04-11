package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.ClientDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PtClientsUiState {
    object Loading : PtClientsUiState()
    data class Success(val clients: List<ClientDto>) : PtClientsUiState()
    data class Error(val message: String) : PtClientsUiState()
}

@HiltViewModel
class PtClientsViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PtClientsUiState>(PtClientsUiState.Loading)
    val uiState: StateFlow<PtClientsUiState> = _uiState.asStateFlow()

    init {
        // loadClients is now called from LaunchedEffect in PtClientsScreen
    }

    fun loadClients() {
        _uiState.value = PtClientsUiState.Loading
        viewModelScope.launch {
            ptRepository.getPtClients(null, 0, 50)
                .onSuccess { response ->
                    _uiState.value = PtClientsUiState.Success(response.content)
                }
                .onFailure { error ->
                    _uiState.value = PtClientsUiState.Error(error.message ?: "Lỗi tải dữ liệu clients")
                }
        }
    }
}
