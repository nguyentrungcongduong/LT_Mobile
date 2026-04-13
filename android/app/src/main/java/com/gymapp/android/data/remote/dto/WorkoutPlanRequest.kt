package com.gymapp.android.data.remote.dto

data class WorkoutPlanRequest(
    val name: String,
    val description: String?,
    val planType: String,
    val targetLevel: String?,
    val assignedTo: String? = null,
    val scheduledDate: String? = null,  //
    val exercises: List<PlanExerciseRequest>
)