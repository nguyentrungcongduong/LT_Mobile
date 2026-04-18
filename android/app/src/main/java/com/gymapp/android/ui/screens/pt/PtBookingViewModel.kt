package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.BatchBookingCreateRequest
import com.gymapp.android.data.remote.api.BatchBookingCreateResponse
import com.gymapp.android.data.remote.api.BookingCreateRequest
import com.gymapp.android.data.remote.api.BookingCreateResponse
import com.gymapp.android.data.remote.api.PtAvailabilityDto
import com.gymapp.android.domain.repository.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class PtBookingUiState {
    object Idle : PtBookingUiState()
    object Loading : PtBookingUiState()
    data class Success(val availabilities: List<PtAvailabilityDto>) : PtBookingUiState()
    data class Error(val message: String) : PtBookingUiState()
    // Single booking (old flow giữ lại)
    data class BookingSuccess(val response: BookingCreateResponse) : PtBookingUiState()
    // Batch booking mới
    data class BatchBookingSuccess(val response: BatchBookingCreateResponse) : PtBookingUiState()
}

@HiltViewModel
class PtBookingViewModel @Inject constructor(
    private val ptRepository: PtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val ptId: String = checkNotNull(savedStateHandle["ptId"])

    private val _uiState = MutableStateFlow<PtBookingUiState>(PtBookingUiState.Idle)
    val uiState: StateFlow<PtBookingUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance().time)
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    // Multi-select: Set của slotId đã chọn
    private val _selectedSlotIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSlotIds: StateFlow<Set<String>> = _selectedSlotIds.asStateFlow()

    private val _selectedProvider = MutableStateFlow<String?>("VNPAY")
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    private val _ptDetail = MutableStateFlow<com.gymapp.android.data.remote.api.PtPublicDto?>(null)
    val ptDetail: StateFlow<com.gymapp.android.data.remote.api.PtPublicDto?> = _ptDetail.asStateFlow()

    private val _viewingMonth = MutableStateFlow(Calendar.getInstance())
    val viewingMonth: StateFlow<Calendar> = _viewingMonth.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadPtDetail()
        loadMonthAvailability()
    }

    private fun loadPtDetail() {
        viewModelScope.launch {
            ptRepository.getPtDetail(ptId)
                .onSuccess { detail -> _ptDetail.value = detail }
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        // Không reset slot đã chọn, user có thể chọn slot từ nhiều ngày
    }

    /** Toggle slot: nếu đã chọn thì bỏ, chưa chọn thì thêm */
    fun toggleSlot(slotId: String) {
        val current = _selectedSlotIds.value.toMutableSet()
        if (current.contains(slotId)) {
            current.remove(slotId)
        } else {
            current.add(slotId)
        }
        _selectedSlotIds.value = current
    }

    fun selectProvider(provider: String) {
        _selectedProvider.value = provider
    }

    fun nextMonth() {
        val next = _viewingMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _viewingMonth.value = next
        loadMonthAvailability()
    }

    fun prevMonth() {
        val prev = _viewingMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _viewingMonth.value = prev
        loadMonthAvailability()
    }

    /** Load toàn bộ lịch trống trong tháng đang xem */
    fun loadMonthAvailability() {
        val month = _viewingMonth.value.clone() as Calendar
        month.set(Calendar.DAY_OF_MONTH, 1)
        val fromStr = dateFormat.format(month.time)

        month.set(Calendar.DAY_OF_MONTH, month.getActualMaximum(Calendar.DAY_OF_MONTH))
        val toStr = dateFormat.format(month.time)

        _uiState.value = PtBookingUiState.Loading
        viewModelScope.launch {
            ptRepository.getAvailability(ptId, fromStr, toStr)
                .onSuccess { availabilities ->
                    _uiState.value = PtBookingUiState.Success(availabilities)
                }
                .onFailure { error ->
                    _uiState.value = PtBookingUiState.Error(error.message ?: "Lỗi tải lịch trống")
                }
        }
    }

    // Alias giữ backward compat
    fun loadAvailability() = loadMonthAvailability()

    /** Đặt nhiều buổi cùng lúc → 1 thanh toán */
    fun confirmBatchBooking() {
        val slotIds = _selectedSlotIds.value.toList()
        if (slotIds.isEmpty()) return
        val provider = _selectedProvider.value ?: "VNPAY"
        _uiState.value = PtBookingUiState.Loading
        viewModelScope.launch {
            ptRepository.createBatchBookings(
                BatchBookingCreateRequest(ptId, slotIds, provider)
            ).onSuccess { response ->
                _uiState.value = PtBookingUiState.BatchBookingSuccess(response)
            }.onFailure { error ->
                _uiState.value = PtBookingUiState.Error(error.message ?: "Lỗi đặt lịch")
            }
        }
    }

    /** Đặt 1 buổi (giữ backward compat) */
    fun confirmBooking() {
        val slotId = _selectedSlotIds.value.firstOrNull() ?: return
        val provider = _selectedProvider.value ?: "VNPAY"
        _uiState.value = PtBookingUiState.Loading
        viewModelScope.launch {
            ptRepository.createBooking(BookingCreateRequest(ptId, slotId, provider))
                .onSuccess { response -> _uiState.value = PtBookingUiState.BookingSuccess(response) }
                .onFailure { error -> _uiState.value = PtBookingUiState.Error(error.message ?: "Lỗi đặt lịch") }
        }
    }

    fun resetUiState() {
        _uiState.value = PtBookingUiState.Idle
    }

    fun clearSelectedSlots() {
        _selectedSlotIds.value = emptySet()
    }
}
