package com.gymapp.android.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.local.Prefs
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.data.remote.api.NotificationApi
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
    private val notificationApi: NotificationApi,
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

    /** Lịch tập sắp tới của USER thường (CONFIRMED, tối đa 3 buổi) */
    private val _userUpcomingBookings = MutableStateFlow<List<BookingDto>>(emptyList())
    val userUpcomingBookings: StateFlow<List<BookingDto>> = _userUpcomingBookings.asStateFlow()

    /** Số thông báo chưa đọc — hiển thị trên badge icon chuông */
    private val _unreadNotifCount = MutableStateFlow(0)
    val unreadNotifCount: StateFlow<Int> = _unreadNotifCount.asStateFlow()

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
                _needSetupGoal.value = !hasSetupLocal && user.role != "ADMIN"

                when (user.role) {
                    "ADMIN" -> fetchAdminStats()
                    "PT"    -> fetchPtStats()
                    else    -> fetchUserUpcomingBookings()
                }

                // Fetch unread count sau khi profile thành công (token đã sẵn sàng)
                fetchUnreadNotifCount()
            }.onFailure {
                _needSetupGoal.value = false
            }
        }
    }

    /** Fetch số thông báo chưa đọc */
    fun fetchUnreadNotifCount() {
        viewModelScope.launch {
            try {
                val resp = notificationApi.getNotifications()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val list = resp.body()!!.data ?: emptyList()
                    _unreadNotifCount.value = list.count { !it.isRead }
                }
            } catch (_: Exception) {}
        }
    }

    /** Gọi khi user đã vào màn thông báo → reset badge về 0 */
    fun clearUnreadNotifCount() {
        _unreadNotifCount.value = 0
    }

    fun onGoalNavigated() {
        _needSetupGoal.value = false
    }

    private fun fetchAdminStats() {
        viewModelScope.launch {
            userRepository.getAdminDashboardStats()
                .onSuccess { stats -> _adminStats.value = stats }
        }
    }

    fun fetchUserUpcomingBookings() {
        viewModelScope.launch {
            ptRepository.getUserBookings(
                status = "CONFIRMED",
                page = 0,
                size = 3
            ).onSuccess { page ->
                _userUpcomingBookings.value = page.content
            }
        }
    }

    fun fetchPtStats() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            ptRepository.getPtBookings(
                status = "CONFIRMED",
                upcomingOnly = true,
                page = 0,
                size = 5
            ).onSuccess { page ->
                val all = page.content
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

            ptRepository.getPtClients(status = null, page = 0, size = 100)
                .onSuccess { page ->
                    _ptStats.value = _ptStats.value.copy(activeClients = page.content.size)
                }
        }
    }
}
