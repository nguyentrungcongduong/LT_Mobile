package com.gymapp.android.ui.screens.training

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.dto.PlanExerciseRequest
import com.gymapp.android.data.remote.dto.WorkoutLogResponse
import com.gymapp.android.data.remote.dto.WorkoutPlanRequest
import com.gymapp.android.data.remote.response.WorkoutPlanResponse
import com.gymapp.android.data.repository.WorkoutRepository
import com.gymapp.android.domain.model.workout.ExerciseUI
import com.gymapp.android.domain.model.workout.WpType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository
) : ViewModel() {

    var customPlans by mutableStateOf<List<WorkoutPlanResponse>>(emptyList())
    var ptPlans by mutableStateOf<List<WorkoutPlanResponse>>(emptyList())
    var recommendedPlans by mutableStateOf<List<WorkoutPlanResponse>>(emptyList())

    var selectedType by mutableStateOf("RECOMMENDED")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var exercises by mutableStateOf<List<ExerciseUI>>(emptyList())

    init {
        loadByType("RECOMMENDED")
    }

    var currentPlan by mutableStateOf<WorkoutPlanResponse?>(null)

    fun fetchPlanById(planId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                currentPlan = repo.getPlanById(planId)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message
            }
            isLoading = false
        }
    }

    fun loadByType(type: String) {
        selectedType = type
        isLoading = true

        viewModelScope.launch {
            try {
                when (type) {
                    "USER_CUSTOM" -> {
                        customPlans = repo.getPlans(WpType.USER_CUSTOM) ?: emptyList()
                        Log.d("WORKOUT", "CUSTOM SIZE: ${customPlans.size}")
                    }
                    "PT_ASSIGNED" -> {
                        ptPlans = repo.getPlans(WpType.PT_ASSIGNED)?:emptyList()
                    }
                    "RECOMMENDED" -> {
                        recommendedPlans = repo.getRecommended()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message
            }

            isLoading = false
        }
    }
    fun loadExercises() {
        viewModelScope.launch {
            try {
                exercises = repo.getExercises()
                Log.d("WORKOUT", "LOAD EXERCISES SIZE: ${exercises.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun createPlan(
        name: String,
        scheduledDate: String?,
        exercises: List<PlanExerciseRequest>
    ) {
        viewModelScope.launch {
            try {
                val request = WorkoutPlanRequest(
                    name = name,
                    description = "Tự tạo",
                    planType = "USER_CUSTOM",
                    targetLevel = "BEGINNER",
                    scheduledDate = scheduledDate,
                    exercises = exercises
                )
                repo.createWorkoutPlan(request)
                loadByType("USER_CUSTOM")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
//workout log
var completedPlanIds by mutableStateOf<Set<String>>(emptySet())
    var workoutLogs by mutableStateOf<List<WorkoutLogResponse>>(emptyList())

    fun completePlan(planId: String) {
        viewModelScope.launch {
            try {
                repo.completePlan(planId)
                completedPlanIds = completedPlanIds + planId
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadWorkoutLogs() {
        viewModelScope.launch {
            try {
                workoutLogs = repo.getWorkoutLogs()
                // khôi phục completedPlanIds từ logs
                completedPlanIds = workoutLogs
                    .filter { it.completed && it.planId != null }
                    .map { it.planId!! }
                    .toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}


