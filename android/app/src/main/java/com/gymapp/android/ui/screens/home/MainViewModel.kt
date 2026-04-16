package com.gymapp.android.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.local.Prefs
import com.gymapp.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
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

    init {
        fetchProfile()
    }
    private val _needSetupGoal = MutableStateFlow(false)
    val needSetupGoal: StateFlow<Boolean> = _needSetupGoal.asStateFlow()
    
    fun fetchProfile() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _userRole.value = user.role
                _userAvatar.value = user.avatarUrl
                _currentUserId.value = user.id

                // Chỉ hiện GoalScreen nếu chưa từng setup (check local Prefs)
                // Không phụ thuộc server vì server có thể trả null dù đã setup → gây loop
                val hasSetupLocal = Prefs.hasSetupGoal(context, user.id)
                _needSetupGoal.value = !hasSetupLocal

                if (user.role == "ADMIN") {
                    fetchAdminStats()
                }
            }
                .onFailure {
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
                .onSuccess { stats ->
                    _adminStats.value = stats
                }
                .onFailure {
                    // Ignore or handle
                }
        }
    }
}
