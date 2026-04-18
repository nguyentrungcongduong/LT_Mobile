package com.gymapp.android.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.NotificationApi
import com.gymapp.android.data.remote.api.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(
        val notifications: List<NotificationDto>,
        val unreadCount: Int
    ) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationApi: NotificationApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            try {
                val resp = notificationApi.getNotifications()
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val list = resp.body()!!.data ?: emptyList()
                    _uiState.value = NotificationUiState.Success(
                        notifications = list,
                        unreadCount   = list.count { !it.isRead }
                    )
                } else {
                    _uiState.value = NotificationUiState.Error("Không tải được thông báo")
                }
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationApi.markAllAsRead()
                // Refresh danh sách
                load()
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                notificationApi.markAsRead(id)
                // Cập nhật local state
                val current = _uiState.value
                if (current is NotificationUiState.Success) {
                    val updated = current.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                    _uiState.value = NotificationUiState.Success(
                        notifications = updated,
                        unreadCount   = updated.count { !it.isRead }
                    )
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }
}
