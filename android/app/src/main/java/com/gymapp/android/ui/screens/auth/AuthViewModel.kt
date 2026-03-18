package com.gymapp.android.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.LoginRequest
import com.gymapp.android.data.remote.api.RegisterRequest
import com.gymapp.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun isLoggedIn(): Boolean = repository.hasToken()

    fun login(request: LoginRequest) {
        if (request.email.isBlank() || request.password.isBlank()) {
            uiState = AuthState.Error("Vui lòng điền đủ email và mật khẩu.")
            return
        }

        uiState = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(request)
            uiState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đã có lỗi xảy ra.")
            }
        }
    }

    fun register(request: RegisterRequest, passConfirm: String) {
        if (request.email.isBlank() || request.password.isBlank() || request.fullName.isBlank()) {
            uiState = AuthState.Error("Vui lòng nhập đủ thông tin.")
            return
        }
        if (request.password != passConfirm) {
            uiState = AuthState.Error("Mật khẩu xác nhận không khớp.")
            return
        }
        if (request.password.length < 8) {
            uiState = AuthState.Error("Mật khẩu phải từ 8 ký tự.")
            return
        }

        uiState = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(request)
            uiState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đã có lỗi xảy ra khi Đăng ký.")
            }
        }
    }

    fun logout() {
        repository.logout()
        uiState = AuthState.Idle
    }

    fun resetState() {
        uiState = AuthState.Idle
    }
}
