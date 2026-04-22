package com.gymapp.android.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.CheckinApi
import com.gymapp.android.data.remote.api.PtApi
import com.gymapp.android.data.remote.api.PtMyProfileDto
import com.gymapp.android.data.remote.api.PtProfileUpdateRequest
import com.gymapp.android.data.remote.dto.checkin.CheckinStatsResponse
import com.gymapp.android.domain.model.User
import com.gymapp.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val ptApi: PtApi,
    private val checkinApi: CheckinApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    // Stats thực từ checkin_logs (USER)
    private val _workoutStats = MutableStateFlow<CheckinStatsResponse?>(null)
    val workoutStats: StateFlow<CheckinStatsResponse?> = _workoutStats.asStateFlow()

    // Stats thực từ backend (PT)
    private val _ptProfile = MutableStateFlow<PtMyProfileDto?>(null)
    val ptProfile: StateFlow<PtMyProfileDto?> = _ptProfile.asStateFlow()

    init {
        loadProfile()
        fetchWorkoutStats()
    }

    fun loadProfile() {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _uiState.value = ProfileUiState.Success(user)
                // Nếu là PT, fetch thêm PT profile stats
                if (user.role == "PT") fetchPtProfile()
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "Đã có lỗi xảy ra")
            }
        }
    }

    fun fetchPtProfile() {
        viewModelScope.launch {
            try {
                val response = ptApi.getMyPtProfile()
                if (response.isSuccessful) {
                    _ptProfile.value = response.body()?.data
                }
            } catch (_: Exception) {
                // Lỗi mạng → giữ null
            }
        }
    }

    fun fetchWorkoutStats() {
        viewModelScope.launch {
            try {
                val response = checkinApi.getMyStats()
                if (response.isSuccessful) {
                    _workoutStats.value = response.body()
                }
            } catch (_: Exception) {
                // Lỗi mạng → giữ null, UI sẽ hiện 0
            }
        }
    }

    fun updateProfile(fullName: String, email: String, phone: String) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        _isUpdating.value = true
        viewModelScope.launch {
            userRepository.updateProfile(fullName, email, phone, currentState.user.avatarUrl).onSuccess { user ->
                _uiState.value = ProfileUiState.Success(user)
                _isUpdating.value = false
            }.onFailure { error ->
                android.util.Log.e("ProfileViewModel", "Update failed: ${error.message}", error)
                _isUpdating.value = false
            }
        }
    }

    fun uploadAvatar(file: File) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        _isUploading.value = true
        viewModelScope.launch {
            userRepository.uploadAvatar(file).onSuccess { avatarUrl ->
                _isUploading.value = false
                val updatedUser = currentState.user.copy(avatarUrl = avatarUrl)
                _uiState.value = ProfileUiState.Success(updatedUser)
            }.onFailure { _ ->
                _isUploading.value = false
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isUpdating.value = true
        viewModelScope.launch {
            userRepository.changePassword(oldPass, newPass)
                .onSuccess {
                    _isUpdating.value = false
                    onSuccess()
                }
                .onFailure { error ->
                    _isUpdating.value = false
                    onError(error.message ?: "Đã có lỗi xảy ra khi đổi mật khẩu")
                }
        }
    }

    fun updatePtProfile(
        pricePerSession: Long?,
        bio: String?,
        yearsExperience: Int?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isUpdating.value = true
        viewModelScope.launch {
            try {
                val response = ptApi.updatePtProfile(
                    PtProfileUpdateRequest(
                        pricePerSession = pricePerSession,
                        bio = bio,
                        yearsExperience = yearsExperience
                    )
                )
                _isUpdating.value = false
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Cập nhật thất bại")
                }
            } catch (e: Exception) {
                _isUpdating.value = false
                onError(e.message ?: "Lỗi kết nối")
            }
        }
    }
}
