package com.gymapp.android.data.remote.dto

data class PlanExerciseRequest(
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val orderIndex: Int,
    val notes: String? = null
)