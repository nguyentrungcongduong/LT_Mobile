package com.gymapp.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userRole = MutableStateFlow<String>("USER")
    val userRole: StateFlow<String> = _userRole.asStateFlow()
    
    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _userRole.value = user.role
                _userAvatar.value = user.avatarUrl
            }
        }
    }
}
