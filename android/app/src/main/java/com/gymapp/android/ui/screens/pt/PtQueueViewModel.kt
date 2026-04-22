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
    data class Success(
        val pending: List<BookingDto>,
        val confirmed: List<BookingDto>,
        val awaitingConfirmation: List<BookingDto>  // cần PT xác nhận
    ) : PtQueueUiState()
    data class Error(val message: String) : PtQueueUiState()
}

// Trạng thái xác nhận buổi tập
sealed class AttendanceState {
    object Idle : AttendanceState()
    object Loading : AttendanceState()
    data class Success(val message: String) : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

@HiltViewModel
class PtQueueViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PtQueueUiState>(PtQueueUiState.Loading)
    val uiState: StateFlow<PtQueueUiState> = _uiState.asStateFlow()

    private val _attendanceState = MutableStateFlow<AttendanceState>(AttendanceState.Idle)
    val attendanceState: StateFlow<AttendanceState> = _attendanceState.asStateFlow()

    fun loadQueue() {
        Log.d("PtQueueViewModel", "loadQueue: Bắt đầu gọi API")
        _uiState.value = PtQueueUiState.Loading
        viewModelScope.launch {
            // Load tất cả bookings (bao gồm cả AWAITING_CONFIRMATION)
            ptRepository.getPtBookings(null, false, 0, 100)
                .onSuccess { response ->
                    Log.d("PtQueueViewModel", "loadQueue: ${response.content.size} bản ghi")
                    val pending = response.content.filter { it.status == "PENDING" }
                    val confirmed = response.content.filter { it.status == "CONFIRMED" }
                    val awaiting = response.content.filter { it.status == "AWAITING_CONFIRMATION" }
                    Log.d("PtQueueViewModel", "Pending=${pending.size}, Confirmed=${confirmed.size}, Awaiting=${awaiting.size}")
                    _uiState.value = PtQueueUiState.Success(pending, confirmed, awaiting)
                }
                .onFailure { error ->
                    Log.e("PtQueueViewModel", "loadQueue thất bại", error)
                    _uiState.value = PtQueueUiState.Error(error.message ?: "Đã có lỗi xảy ra")
                }
        }
    }

    /**
     * PT xác nhận học viên có đến tập không.
     * attended = true  → COMPLETED (PT nhận tiền)
     * attended = false → NO_SHOW  (user mất tiền, PT không nhận)
     */
    fun confirmAttendance(bookingId: String, attended: Boolean) {
        _attendanceState.value = AttendanceState.Loading
        viewModelScope.launch {
            ptRepository.confirmAttendance(bookingId, attended)
                .onSuccess {
                    val msg = if (attended) "Xác nhận học viên đã tập!" else "Đã đánh dấu vắng mặt"
                    _attendanceState.value = AttendanceState.Success(msg)
                    loadQueue() // refresh danh sách
                }
                .onFailure { error ->
                    _attendanceState.value = AttendanceState.Error(error.message ?: "Xác nhận thất bại")
                }
        }
    }

    fun resetAttendanceState() {
        _attendanceState.value = AttendanceState.Idle
    }
}
