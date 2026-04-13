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

    init {
        loadUserRole()
    }
    private val _needSetupGoal = MutableStateFlow(false)
    val needSetupGoal: StateFlow<Boolean> = _needSetupGoal.asStateFlow()
    private fun loadUserRole() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _userRole.value = user.role
                val needFromServer =
                    user.experienceLevel == null ||
                            user.fitnessGoal == null

                val hasSetupLocal =
                    Prefs.hasSetupGoal(context, user.id)

                _needSetupGoal.value = needFromServer && !hasSetupLocal
            }
                .onFailure {
                    _needSetupGoal.value = false
                }

        }
    }
}
