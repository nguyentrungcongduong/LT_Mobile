package com.gymapp.android.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.CheckinApi
import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckinLogUiState {
    object Loading : CheckinLogUiState()
    data class Success(val logs: List<CheckinLogResponse>, val total: Long) : CheckinLogUiState()
    data class Error(val message: String) : CheckinLogUiState()
}

@HiltViewModel
class AdminCheckinLogViewModel @Inject constructor(
    private val checkinApi: CheckinApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckinLogUiState>(CheckinLogUiState.Loading)
    val uiState: StateFlow<CheckinLogUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CheckinLogUiState.Loading
            try {
                val response = checkinApi.getCheckinLogs(page = 0, size = 100)
                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = CheckinLogUiState.Success(
                        logs = body?.content ?: emptyList(),
                        total = body?.totalElements ?: 0L
                    )
                } else {
                    _uiState.value = CheckinLogUiState.Error("Lỗi ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = CheckinLogUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }
}
