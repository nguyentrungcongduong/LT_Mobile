package com.gymapp.android.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.local.Prefs
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.domain.repository.PtRepository
import com.gymapp.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PtDashboardStats(
    val todaySessions: Int = 0,
    val activeClients: Int = 0,
    val upcomingBookings: List<BookingDto> = emptyList()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val ptRepository: PtRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _userRole = MutableStateFlow<String>("USER")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _adminStats = MutableStateFlow<com.gymapp.android.data.remote.api.AdminDashboardStatsDto?>(null)
    val adminStats: StateFlow<com.gymapp.android.data.remote.api.AdminDashboardStatsDto?> = _adminStats.asStateFlow()

    private val _ptStats = MutableStateFlow<PtDashboardStats>(PtDashboardStats())
    val ptStats: StateFlow<PtDashboardStats> = _ptStats.asStateFlow()

    private val _needSetupGoal = MutableStateFlow(false)
    val needSetupGoal: StateFlow<Boolean> = _needSetupGoal.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _userRole.value = user.role
                _userAvatar.value = user.avatarUrl
                _currentUserId.value = user.id

                val hasSetupLocal = Prefs.hasSetupGoal(context, user.id)
                _needSetupGoal.value = !hasSetupLocal

                when (user.role) {
                    "ADMIN" -> fetchAdminStats()
                    "PT" -> fetchPtStats()
                }
            }.onFailure {
                _needSetupGoal.value = false
            }
        }
    }

    /** Gọi sau khi đã navigate đến GoalScreen để tránh trigger lại */
    fun onGoalNavigated() {
        _needSetupGoal.value = false
    }

    private fun fetchAdminStats() {
        viewModelScope.launch {
            userRepository.getAdminDashboardStats()
                .onSuccess { stats -> _adminStats.value = stats }
        }
    }

    fun fetchPtStats() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Lịch sắp tới (upcoming = true, lấy 5 cái đầu)
            ptRepository.getPtBookings(
                status = "CONFIRMED",
                upcomingOnly = true,
                page = 0,
                size = 5
            ).onSuccess { page ->
                val all = page.content

                // Ca hôm nay = số booking có scheduledAt là hôm nay
                val todayCount = all.count { booking ->
                    val bookingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(booking.scheduledAt)
                    bookingDate == todayStr
                }

                _ptStats.value = _ptStats.value.copy(
                    todaySessions = todayCount,
                    upcomingBookings = all
                )
            }

            // 2. Clients active (CONFIRMED + COMPLETED)
            ptRepository.getPtClients(
                status = null,
                page = 0,
                size = 100
            ).onSuccess { page ->
                _ptStats.value = _ptStats.value.copy(
                    activeClients = page.content.size
                )
            }
        }
    }
}
