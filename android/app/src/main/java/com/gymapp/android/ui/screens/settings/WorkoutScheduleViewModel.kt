package com.gymapp.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.SaveWorkoutScheduleRequest
import com.gymapp.android.data.remote.api.WorkoutScheduleApi
import com.gymapp.android.data.remote.api.WorkoutScheduleDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutScheduleUiState(
    val schedules: Map<String, String> = emptyMap(),  // dayOfWeek -> remindTime "HH:mm"
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

val ALL_DAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
val DAY_LABELS = mapOf(
    "MON" to "Thứ Hai",
    "TUE" to "Thứ Ba",
    "WED" to "Thứ Tư",
    "THU" to "Thứ Năm",
    "FRI" to "Thứ Sáu",
    "SAT" to "Thứ Bảy",
    "SUN" to "Chủ Nhật"
)

@HiltViewModel
class WorkoutScheduleViewModel @Inject constructor(
    private val scheduleApi: WorkoutScheduleApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutScheduleUiState())
    val uiState: StateFlow<WorkoutScheduleUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val resp = scheduleApi.getSchedules()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val map = resp.body()!!.data
                        ?.associate { it.dayOfWeek to it.remindTime.take(5) }   // "HH:mm"
                        ?: emptyMap()
                    _uiState.value = _uiState.value.copy(schedules = map, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false,
                    errorMessage = e.message ?: "Lỗi kết nối")
            }
        }
    }

    /**
     * Bật/tắt ngày: nếu đã có trong map → xóa; chưa có → thêm với remindTime mặc định
     */
    fun toggleDay(day: String) {
        val current = _uiState.value.schedules.toMutableMap()
        if (current.containsKey(day)) {
            current.remove(day)
        } else {
            current[day] = "06:00"
        }
        _uiState.value = _uiState.value.copy(schedules = current)
    }

    /**
     * Cập nhật giờ nhắc cho một ngày
     */
    fun setRemindTime(day: String, time: String) {
        val current = _uiState.value.schedules.toMutableMap()
        if (current.containsKey(day)) {
            current[day] = time
            _uiState.value = _uiState.value.copy(schedules = current)
        }
    }

    /**
     * Lưu tất cả thay đổi lên backend
     */
    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val schedules = _uiState.value.schedules
                // Lưu từng ngày đang bật
                ALL_DAYS.forEach { day ->
                    val time = schedules[day]
                    if (time != null) {
                        scheduleApi.saveSchedule(day, SaveWorkoutScheduleRequest(day, time))
                    } else {
                        // Xóa ngày không còn trong schedules
                        scheduleApi.deleteSchedule(day)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Đã lưu lịch tập!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Lưu thất bại: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
