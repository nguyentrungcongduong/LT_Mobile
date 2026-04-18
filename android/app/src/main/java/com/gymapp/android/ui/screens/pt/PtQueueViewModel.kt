package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

sealed class PtQueueUiState {
    object Loading : PtQueueUiState()
    data class Success(val pending: List<BookingDto>, val confirmed: List<BookingDto>) : PtQueueUiState()
    data class Error(val message: String) : PtQueueUiState()
}

@HiltViewModel
class PtQueueViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PtQueueUiState>(PtQueueUiState.Loading)
    val uiState: StateFlow<PtQueueUiState> = _uiState.asStateFlow()

    init {
        // loadQueue is now called from LaunchedEffect in PtQueueScreen
    }

    fun loadQueue() {
        Log.d("PtQueueViewModel", "loadQueue: Bắt đầu gọi API")
        _uiState.value = PtQueueUiState.Loading
        viewModelScope.launch {
            // Load upcoming bookings regardless of status
            ptRepository.getPtBookings(null, true, 0, 50)
                .onSuccess { response ->
                    Log.d("PtQueueViewModel", "loadQueue: Gọi API thành công, nhận được ${response.content.size} bản ghi")
                    response.content.forEach {
                        Log.d("PtQueueViewModel", "Booking Item: ID=${it.id}, Status=${it.status}, Time=${it.scheduledAt}, User=${it.userName}")
                    }
                    val pending = response.content.filter { it.status == "PENDING" }
                    val confirmed = response.content.filter { it.status == "CONFIRMED" }
                    Log.d("PtQueueViewModel", "Filtered: Pending=${pending.size}, Confirmed=${confirmed.size}")
                    _uiState.value = PtQueueUiState.Success(pending, confirmed)
                }
                .onFailure { error ->
                    Log.e("PtQueueViewModel", "loadQueue: Gọi API thất bại", error)
                    _uiState.value = PtQueueUiState.Error(error.message ?: "Đã có lỗi xảy ra")
                }
        }
    }
}
