package com.gymapp.android.data.remote.dto

data class ExerciseResponse(
    val id: String,
    val name: String,
    val muscleGroup: String?,
    val description: String? = null,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null
)