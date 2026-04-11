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
import javax.inject.Inject

sealed class UserBookingsUiState {
    object Loading : UserBookingsUiState()
    data class Success(val bookings: List<BookingDto>) : UserBookingsUiState()
    data class Error(val message: String) : UserBookingsUiState()
}

@HiltViewModel
class UserBookingsViewModel @Inject constructor(
    private val ptRepository: PtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserBookingsUiState>(UserBookingsUiState.Loading)
    val uiState: StateFlow<UserBookingsUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Sắp tới, 1: Đã xong, 2: Đã hủy
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadBookings()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        loadBookings()
    }

    fun loadBookings() {
        val status = when (_selectedTab.value) {
            0 -> "CONFIRMED"
            1 -> "COMPLETED"
            2 -> "CANCELLED"
            else -> "CONFIRMED"
        }

        _uiState.value = UserBookingsUiState.Loading
        viewModelScope.launch {
            val page = 0
            val size = 50
            ptRepository.getUserBookings(status, page, size)
                .onSuccess { response ->
                    _uiState.value = UserBookingsUiState.Success(response.content)
                }
                .onFailure { error ->
                    _uiState.value = UserBookingsUiState.Error(error.message ?: "Đã có lỗi xảy ra")
                }
        }
    }
}
