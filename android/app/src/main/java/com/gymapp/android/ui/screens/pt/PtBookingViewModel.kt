package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    data class BookingSuccess(val response: BookingCreateResponse) : PtBookingUiState()
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

    private val _selectedSlotId = MutableStateFlow<String?>(null)
    val selectedSlotId: StateFlow<String?> = _selectedSlotId.asStateFlow()

    private val _ptDetail = MutableStateFlow<com.gymapp.android.data.remote.api.PtPublicDto?>(null)
    val ptDetail: StateFlow<com.gymapp.android.data.remote.api.PtPublicDto?> = _ptDetail.asStateFlow()

    private val _viewingMonth = MutableStateFlow(Calendar.getInstance())
    val viewingMonth: StateFlow<Calendar> = _viewingMonth.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadPtDetail()
        loadAvailability()
    }

    private fun loadPtDetail() {
        viewModelScope.launch {
            ptRepository.getPtDetail(ptId)
                .onSuccess { detail ->
                    _ptDetail.value = detail
                }
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        _selectedSlotId.value = null
        loadAvailability()
    }

    fun selectSlot(slotId: String) {
        _selectedSlotId.value = slotId
    }

    fun nextMonth() {
        val next = _viewingMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _viewingMonth.value = next
    }

    fun prevMonth() {
        val prev = _viewingMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _viewingMonth.value = prev
    }

    fun loadAvailability() {
        val selectedDateVal = _selectedDate.value
        val toDateStr = dateFormat.format(selectedDateVal)
        
        // Calculate fromDate (Selected Date - 1 day)
        val cal = Calendar.getInstance().apply { 
            time = selectedDateVal
            add(Calendar.DAY_OF_YEAR, -1) 
        }
        val fromDateStr = dateFormat.format(cal.time)
        
        _uiState.value = PtBookingUiState.Loading
        viewModelScope.launch {
            ptRepository.getAvailability(ptId, fromDateStr, toDateStr)
                .onSuccess { availabilities: List<PtAvailabilityDto> ->
                    _uiState.value = PtBookingUiState.Success(availabilities)
                }
                .onFailure { error ->
                    _uiState.value = PtBookingUiState.Error(error.message ?: "Lỗi tải lịch trống")
                }
        }
    }

    fun confirmBooking() {
        val slotId = _selectedSlotId.value ?: return
        _uiState.value = PtBookingUiState.Loading
        viewModelScope.launch {
            ptRepository.createBooking(BookingCreateRequest(ptId, slotId))
                .onSuccess { response ->
                    _uiState.value = PtBookingUiState.BookingSuccess(response)
                }
                .onFailure { error ->
                    _uiState.value = PtBookingUiState.Error(error.message ?: "Lỗi đặt lịch")
                }
        }
    }
}
