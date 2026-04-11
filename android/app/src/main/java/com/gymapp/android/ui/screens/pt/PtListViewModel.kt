package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.PtPublicDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PtListUiState {
    object Loading : PtListUiState()
    data class Success(val pts: List<PtPublicDto>) : PtListUiState()
    data class Error(val message: String) : PtListUiState()
}

@HiltViewModel
class PtListViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PtListUiState>(PtListUiState.Loading)
    val uiState: StateFlow<PtListUiState> = _uiState.asStateFlow()

    init {
        loadPts()
    }

    fun loadPts() {
        _uiState.value = PtListUiState.Loading
        viewModelScope.launch {
            ptRepository.getPtList(0, 50)
                .onSuccess { response: com.gymapp.android.data.remote.api.PageResponse<PtPublicDto> ->
                    println(">>> PtList Success: ${response.content.size} items")
                    _uiState.value = PtListUiState.Success(response.content)
                }
                .onFailure { error ->
                    println(">>> PtList Error: ${error.message}")
                    _uiState.value = PtListUiState.Error(error.message ?: "Lỗi tải danh sách PT")
                }
        }
    }
}
