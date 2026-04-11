package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.CancelBookingRequest
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

sealed class PtBookingCancelUiState {
    object Idle : PtBookingCancelUiState()
    object Loading : PtBookingCancelUiState()
    data class Success(val message: String) : PtBookingCancelUiState()
    data class Error(val message: String) : PtBookingCancelUiState()
}

data class BookingInfo(
    val id: String,
    val ptName: String,
    val scheduledAt: OffsetDateTime?,
    val amount: BigDecimal
)

@HiltViewModel
class PtBookingCancelViewModel @Inject constructor(
    private val ptRepository: PtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String? = savedStateHandle["bookingId"]
    private val ptName: String? = savedStateHandle["ptName"]
    private val timestampStr: String? = savedStateHandle["scheduledAt"]
    private val amountStr: String? = savedStateHandle["amount"]

    private val _uiState = MutableStateFlow<PtBookingCancelUiState>(PtBookingCancelUiState.Idle)
    val uiState: StateFlow<PtBookingCancelUiState> = _uiState.asStateFlow()

    private val _bookingInfo = MutableStateFlow<BookingInfo?>(null)
    val bookingInfo: StateFlow<BookingInfo?> = _bookingInfo.asStateFlow()

    init {
        try {
            if (bookingId != null && ptName != null && amountStr != null) {
                val timestamp = timestampStr?.toLongOrNull() ?: 0L
                val scheduledAt = if (timestamp > 0L) {
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                } else null
                
                val amount = BigDecimal(amountStr)
                _bookingInfo.value = BookingInfo(bookingId, ptName, scheduledAt, amount)
            } else {
                _uiState.value = PtBookingCancelUiState.Error("Thiếu thông tin lịch hẹn")
            }
        } catch (e: Exception) {
            _uiState.value = PtBookingCancelUiState.Error("Lỗi dữ liệu: ${e.message}")
        }
    }

    fun cancelBooking(reason: String) {
        val id = bookingId ?: return
        _uiState.value = PtBookingCancelUiState.Loading
        viewModelScope.launch {
            ptRepository.cancelBooking(id, CancelBookingRequest(reason))
                .onSuccess {
                    _uiState.value = PtBookingCancelUiState.Success("Hủy lịch thành công")
                }
                .onFailure { error ->
                    _uiState.value = PtBookingCancelUiState.Error(error.message ?: "Lỗi khi hủy lịch")
                }
        }
    }
}
