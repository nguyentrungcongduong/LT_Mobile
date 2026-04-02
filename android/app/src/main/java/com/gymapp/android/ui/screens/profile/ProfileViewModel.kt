package com.gymapp.android.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _uiState.value = ProfileUiState.Success(user)
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "Đã có lỗi xảy ra")
            }
        }
    }

    fun updateProfile(fullName: String, phone: String) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        _isUpdating.value = true
        viewModelScope.launch {
            userRepository.updateProfile(fullName, phone, currentState.user.avatarUrl).onSuccess { user ->
                _uiState.value = ProfileUiState.Success(user)
                _isUpdating.value = false
            }.onFailure { _ ->
                _isUpdating.value = false
                // you could use an event channel for toast errors, but this is simple enough
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
                // Update profile with new avatar URL
                val updatedUser = currentState.user.copy(avatarUrl = avatarUrl)
                _uiState.value = ProfileUiState.Success(updatedUser)
            }.onFailure { _ ->
                _isUploading.value = false
            }
        }
    }
}
