package com.gymapp.android.data.repository

import com.gymapp.android.data.remote.api.PageResponse
import com.gymapp.android.data.remote.api.WorkoutApi
import com.gymapp.android.data.remote.dto.WorkoutLogRequest
import com.gymapp.android.data.remote.dto.WorkoutLogResponse
import com.gymapp.android.data.remote.dto.WorkoutPlanRequest
import com.gymapp.android.data.remote.response.WorkoutPlanResponse
import com.gymapp.android.domain.model.workout.ExerciseUI
import com.gymapp.android.domain.model.workout.WpType
import java.time.LocalDate
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val api: WorkoutApi
) {

    suspend fun getPlans(type: WpType): List<WorkoutPlanResponse> {
        val response = api.getPlans(type.name)
        return response.content ?: emptyList() //  FIX NULL
    }

    suspend fun getRecommended(): List<WorkoutPlanResponse> {
        return api.getRecommended().content ?: emptyList()
    }
    
    suspend fun getPlanById(id: String): WorkoutPlanResponse? {
        return try {
            api.getPlanById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun createWorkoutPlan(request: WorkoutPlanRequest) {
        api.createWorkoutPlan(request)
    }
    suspend fun getExercises(): List<ExerciseUI> {
        return api.getExercises().map {
            ExerciseUI(
                id = it.id,
                name = it.name
            )
        }
    }
    suspend fun completePlan(planId: String) {
        val today = LocalDate.now().toString()
        api.createWorkoutLog(
            WorkoutLogRequest(
                planId = planId,
                logDate = today,
                completed = true
            )
        )
    }

    suspend fun getWorkoutLogs(): List<WorkoutLogResponse> {
        return api.getWorkoutLogs().content ?: emptyList()
    }
}