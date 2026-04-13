package com.gymapp.android.data.remote.response

import com.gymapp.android.domain.model.workout.WpType

data class WorkoutPlanResponse(
    val id: String,
    val name: String,
    val description: String?,
    val planType: String?,
    val targetLevel: String? = null,
    val assignedToName: String? = null,
    val createdByName: String? = null,
    val scheduledDate: String? = null,
    val exercises: List<PlanExerciseResponse> = emptyList()
)