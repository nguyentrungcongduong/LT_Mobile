package com.gymapp.android.data.remote.response

data class PlanExerciseResponse(
    val exerciseName: String,
    val muscleGroup: String?,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val orderIndex: Int,
    val notes: String?
)