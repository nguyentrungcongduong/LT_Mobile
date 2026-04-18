package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.PtApi
import com.gymapp.android.data.remote.api.PtAvailabilityDto
import com.gymapp.android.data.remote.api.CreateAvailabilityRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class PtScheduleUiState {
    object Idle : PtScheduleUiState()
    object Loading : PtScheduleUiState()
    data class Success(val slots: List<PtAvailabilityDto>) : PtScheduleUiState()
    data class Error(val message: String) : PtScheduleUiState()
}

@HiltViewModel
class PtScheduleManagementViewModel @Inject constructor(
    private val ptApi: PtApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<PtScheduleUiState>(PtScheduleUiState.Idle)
    val uiState: StateFlow<PtScheduleUiState> = _uiState.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _mySlots = MutableStateFlow<List<PtAvailabilityDto>>(emptyList())
    val mySlots: StateFlow<List<PtAvailabilityDto>> = _mySlots.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // ID của PT hiện login — sẽ cần để gọi GET availability
    // Backend lấy từ JWT nên không cần truyền khi POST
    // Nhưng cần để GET (cần biết ptId) → tạm thời dùng profile endpoint
    private var currentPtId: String? = null

    fun setCurrentPtId(ptId: String) {
        if (ptId.isBlank()) return
        currentPtId = ptId
        loadSlots()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun loadSlots() {
        val ptId = currentPtId
        if (ptId.isNullOrBlank()) return
        val date = _selectedDate.value
        val from = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val to = date.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)

        _uiState.value = PtScheduleUiState.Loading
        viewModelScope.launch {
            try {
                val response = ptApi.getAvailability(ptId, from, to)
                if (response.isSuccessful) {
                    val slots = response.body()?.data ?: emptyList()
                    _mySlots.value = slots
                    _uiState.value = PtScheduleUiState.Success(slots)
                } else {
                    _uiState.value = PtScheduleUiState.Error("Không thể tải lịch")
                }
            } catch (e: Exception) {
                _uiState.value = PtScheduleUiState.Error(e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun createSlot(
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val startTime = String.format("%02d:%02d", startHour, startMinute)
        val endTime = String.format("%02d:%02d", endHour, endMinute)
        val availableDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        _isCreating.value = true
        viewModelScope.launch {
            try {
                val response = ptApi.createAvailability(
                    CreateAvailabilityRequest(
                        availableDate = availableDate,
                        startTime = startTime,
                        endTime = endTime
                    )
                )
                _isCreating.value = false
                if (response.isSuccessful) {
                    onSuccess()
                    loadSlots()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Tạo slot thất bại"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                _isCreating.value = false
                onError(e.message ?: "Lỗi kết nối")
            }
        }
    }
}
