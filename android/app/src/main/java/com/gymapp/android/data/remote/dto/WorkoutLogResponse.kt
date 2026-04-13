package com.gymapp.android.data.remote.dto

data class WorkoutLogResponse(
    val id: String,
    val planId: String?,
    val planName: String?,
    val logDate: String,
    val durationMinutes: Int?,
    val notes: String?,
    val completed: Boolean,
    val createdAt: String?
)