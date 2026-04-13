package com.gymapp.android.ui.screens.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.local.Prefs
import com.gymapp.android.data.remote.dto.user.UpdateUserGoalRequest
import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal
import com.gymapp.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    var experienceLevel by mutableStateOf(ExperienceLevel.BEGINNER)
    var fitnessGoal by mutableStateOf(FitnessGoal.WEIGHT_LOSS)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            userRepository.updateGoal(
                UpdateUserGoalRequest(
                    experienceLevel,
                    fitnessGoal
                )
            ).onSuccess {
                val user = userRepository.getProfile().getOrNull()
                user?.let {
                    Prefs.setHasSetupGoal(context, it.id, true)
                }
                onSuccess()
            }.onFailure {
                errorMessage = it.message
            }

            isLoading = false
        }
    }
}