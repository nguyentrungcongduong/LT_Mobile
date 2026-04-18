package com.gymapp.android.data.remote.dto

data class WorkoutLogRequest(
    val planId: String?,
    val logDate: String,
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val completed: Boolean = true
)